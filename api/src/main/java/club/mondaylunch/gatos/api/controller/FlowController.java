package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.exception.InvalidBodyException;
import club.mondaylunch.gatos.api.exception.flow.InvalidConnectionException;
import club.mondaylunch.gatos.api.exception.flow.InvalidNodeSettingException;
import club.mondaylunch.gatos.api.exception.flow.InvalidNodeTypeException;
import club.mondaylunch.gatos.api.exception.flow.NodeNotFoundException;
import club.mondaylunch.gatos.api.repository.FlowRepository;
import club.mondaylunch.gatos.api.repository.UserRepository;
import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.executor.GraphExecutor;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.WebhookStartNodeInput;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {
    private final FlowRepository flowRepository;
    private final UserRepository userRepository;

    @Autowired
    public FlowController(FlowRepository flowRepository, UserRepository userRepository) {
        this.flowRepository = flowRepository;
        this.userRepository = userRepository;
    }

    private record BasicFlowInfo(
        @JsonProperty("_id") UUID id,
        String name,
        String description,
        @JsonProperty("author_id") UUID authorId
    ) {
        BasicFlowInfo(Flow flow) {
            this(flow.getId(), flow.getName(), flow.getDescription(), flow.getAuthorId());
        }
    }

    /**
     * Get all flows of the user.
     *
     * @return A list of flows.
     * Does not include information about the graph.
     */
    @GetMapping
    public List<BasicFlowInfo> getFlows(@RequestHeader(name = "x-user-email") String userEmail) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        return Flow.objects.get("author_id", user.getId())
            .stream()
            .map(BasicFlowInfo::new)
            .toList();
    }

    /**
     * Gets a specific flow.
     *
     * @return The flow.
     */
    @GetMapping(value = "{flowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFlow(@RequestHeader(name = "x-user-email") String userEmail, @PathVariable("flowId") UUID flowId) {
        var user = this.userRepository.getOrCreateUser(userEmail);
        return this.flowRepository.getFlow(user, flowId).toJson();
    }

    private record BodyAddFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    /**
     * Creates a new flow.
     *
     * @return The created flow.
     * Does not include information about the graph.
     */
    @PostMapping
    public BasicFlowInfo addFlow(@RequestHeader(name = "x-user-email") String userEmail, @Valid @RequestBody BodyAddFlow data) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = new Flow();
        flow.setName(data.name);
        flow.setAuthorId(user.getId());
        flow.setDescription(data.description);

        Flow.objects.insert(flow);
        return new BasicFlowInfo(flow);
    }

    private record BodyUpdateFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    /**
     * Updates a flow.
     *
     * @return The updated flow.
     * Does not include information about the graph.
     */
    @PatchMapping("{flowId}")
    public BasicFlowInfo updateFlow(
        @RequestHeader(name = "x-user-email") String userEmail, @PathVariable UUID flowId,
                                    @Valid @RequestBody BodyUpdateFlow data) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);

        var partial = new Flow();
        partial.setName(data.name);
        partial.setDescription(data.description);
        partial.setGraph(null);

        Flow.objects.update(flow.getId(), partial);
        var updated = Flow.objects.get(flow.getId());
        return new BasicFlowInfo(updated);
    }

    /**
     * Deletes a flow.
     */
    @DeleteMapping("{flowId}")
    public void deleteFlow(@RequestHeader(name = "x-user-email") String userEmail, @PathVariable UUID flowId) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        Flow.objects.delete(flow.getId());
    }

    // Graph operations

    private record BodyAddNode(
        @JsonProperty("type") String nodeType
    ) {
    }

    /**
     * Gets a node from the flow graph.
     *
     * @return The node.
     */
    @GetMapping(value = "{flowId}/nodes/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getNode(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var node = graph.getNode(nodeId)
            .orElseThrow(NodeNotFoundException::new);
        return SerializationUtils.toJson(node);
    }

    /**
     * Adds a node to the flow graph.
     *
     * @return The added node.
     */
    @PostMapping(value = "{flowId}/nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addNode(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @Valid @RequestBody BodyAddNode body
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var nodeType = NodeType.REGISTRY.get(body.nodeType)
            .orElseThrow(InvalidNodeTypeException::new);
        var node = graph.addNode(nodeType);
        Flow.objects.updateGraph(flow);
        return SerializationUtils.toJson(node);
    }

    /**
     * Modifies a node's settings.
     *
     * @return The node with the updated settings.
     */
    @PatchMapping(value = "{flowId}/nodes/{nodeId}/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    public String modifyNodeSettings(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId,
        @RequestBody String body
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        if (!graph.containsNode(nodeId)) {
            throw new NodeNotFoundException();
        }
        Map<String, DataBox<?>> newSettings;
        try {
            @SuppressWarnings("unchecked")
            var type = (Class<DataBox<?>>) (Object) DataBox.class;
            newSettings = SerializationUtils.readMap(body, Function.identity(), type);
        } catch (Exception e) {
            throw new InvalidBodyException();
        }
        for (var entry : newSettings.entrySet()) {
            var key = entry.getKey();
            var dataBox = entry.getValue();
            graph.modifyNode(nodeId, node -> {
                try {
                    return node.modifySetting(key, dataBox);
                } catch (Exception e) {
                    throw new InvalidNodeSettingException(e.getMessage());
                }
            });
        }
        Flow.objects.updateGraph(flow);
        var node = graph.getNode(nodeId).orElseThrow();
        return SerializationUtils.toJson(node);
    }

    /**
     * Deletes a node from the flow graph.
     */
    @DeleteMapping("{flowId}/nodes/{nodeId}")
    public void deleteNode(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        graph.removeNode(nodeId);
        Flow.objects.updateGraph(flow);
    }

    /**
     * Gets all connections for a node.
     *
     * @return The connections.
     */
    @GetMapping(value = "{flowId}/connections/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getConnections(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        if (!graph.containsNode(nodeId)) {
            throw new NodeNotFoundException();
        }
        var connectionsJson = new JsonArray();
        var connections = graph.getConnectionsForNode(nodeId);
        for (var connection : connections) {
            var connectionJsonString = SerializationUtils.toJson(connection);
            var connectionJson = JsonParser.parseString(connectionJsonString);
            connectionsJson.add(connectionJson);
        }
        return connectionsJson.toString();
    }

    private record BodyConnection(
        @JsonProperty("from_node_id") UUID fromNodeId,
        @JsonProperty("from_name") String fromName,
        @JsonProperty("to_node_id") UUID toNodeId,
        @JsonProperty("to_name") String toName
    ) {
    }

    /**
     * Adds a connection between two nodes.
     *
     * @return The added connection.
     */
    @PostMapping(value = "{flowId}/connections", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addConnection(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @RequestBody BodyConnection body
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var connection = createConnection(graph, body);
        try {
            graph.addConnection(connection);
        } catch (Exception e) {
            throw new InvalidConnectionException(e.getMessage());
        }
        Flow.objects.updateGraph(flow);
        return SerializationUtils.toJson(connection);
    }

    /**
     * Deletes a connection between two nodes.
     */
    @DeleteMapping("{flowId}/connections")
    public void deleteConnection(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @RequestBody BodyConnection body
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var connection = createConnection(graph, body);
        try {
            graph.removeConnection(connection);
        } catch (Exception e) {
            throw new InvalidConnectionException(e.getMessage());
        }
        Flow.objects.updateGraph(flow);
    }

    private static NodeConnection<?> createConnection(Graph graph, BodyConnection body) {
        var fromNode = graph.getNode(body.fromNodeId)
            .orElseThrow(() -> new NodeNotFoundException(body.fromNodeId));
        var toNode = graph.getNode(body.toNodeId)
            .orElseThrow(() -> new NodeNotFoundException(body.toNodeId));
        try {
            return NodeConnection.create(
                fromNode,
                body.fromName,
                toNode,
                body.toName
            );
        } catch (Exception e) {
            throw new InvalidConnectionException(e.getMessage(), e);
        }
    }

    /**
     * Gets a node's metadata.
     *
     * @return The metadata.
     */
    @GetMapping(value = "{flowId}/nodes/{nodeId}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMetadata(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        if (!graph.containsNode(nodeId)) {
            throw new NodeNotFoundException();
        }
        var metadata = graph.getOrCreateMetadataForNode(nodeId);
        return SerializationUtils.toJson(metadata);
    }

    /**
     * Modifies a node's metadata.
     *
     * @return The updated metadata.
     */
    @PatchMapping(value = "{flowId}/nodes/{nodeId}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public String modifyNodeMetadata(
        @RequestHeader("x-user-email") String userEmail,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId,
        @RequestBody NodeMetadata metadata
    ) {
        User user = this.userRepository.getOrCreateUser(userEmail);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        if (!graph.containsNode(nodeId)) {
            throw new NodeNotFoundException();
        }
        graph.setMetadata(nodeId, metadata);
        Flow.objects.updateGraph(flow);
        return SerializationUtils.toJson(metadata);
    }

    /**
     * Executes a flow.
     *
     * @return The flow output.
     */
    @PostMapping(value = "{flowId}/run/{startNodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeFlow(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @PathVariable UUID startNodeId,
        @RequestBody(required = false) @Nullable String input
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var startNode = graph.getNode(startNodeId)
            .orElseThrow(() -> new NodeNotFoundException(startNodeId));
        boolean isWebhookStart = NodeType.REGISTRY.get("webhook_start")
            .map(nodeType -> startNode.type().equals(nodeType))
            .orElse(false);
        if (!isWebhookStart) {
            throw new InvalidNodeTypeException("Node with ID " + startNodeId + " is not a webhook start node.");
        }
        var executor = new GraphExecutor(graph);
        var executeFunction = executor.execute(startNodeId);
        JsonObject inputJson;
        if (input == null) {
            inputJson = new JsonObject();
        } else {
            var inputJsonElement = JsonParser.parseString(input);
            if (inputJsonElement.isJsonObject()) {
                inputJson = inputJsonElement.getAsJsonObject();
            } else {
                throw new InvalidBodyException("Body must be a JSON object");
            }
        }
        AtomicReference<?> outputReference = new AtomicReference<>();
        var webhookStartInput = new WebhookStartNodeInput(inputJson, outputReference);
        executeFunction.accept(webhookStartInput);
        var output = outputReference.get();
        if (output == null) {
            return new JsonObject().toString();
        } else {
            return SerializationUtils.toJson(output);
        }
    }
}
