package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
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

import club.mondaylunch.gatos.api.exception.flow.InvalidConnectionException;
import club.mondaylunch.gatos.api.exception.flow.InvalidDataTypeException;
import club.mondaylunch.gatos.api.exception.flow.InvalidNodeTypeException;
import club.mondaylunch.gatos.api.exception.flow.NodeNotFoundException;
import club.mondaylunch.gatos.api.repository.FlowRepository;
import club.mondaylunch.gatos.api.repository.LoginRepository;
import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/flows")
public class FlowController {
    private final LoginRepository userRepository;
    private final FlowRepository flowRepository;

    public FlowController(LoginRepository repository, FlowRepository flowRepository) {
        this.userRepository = repository;
        this.flowRepository = flowRepository;
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

    @GetMapping
    public List<BasicFlowInfo> getFlows(@RequestHeader("x-auth-token") String token) {
        var user = this.userRepository.authenticateUser(token);
        return Flow.objects.get("author_id", user.getId())
            .stream()
            .map(BasicFlowInfo::new)
            .toList();
    }

    @GetMapping(value = "{flowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFlow(@PathVariable("flowId") UUID flowId, @RequestHeader("x-auth-token") String token) {
        User user = this.userRepository.authenticateUser(token);
        return this.flowRepository.getFlow(user, flowId).toJson();
    }

    private record BodyAddFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PostMapping
    public BasicFlowInfo addFlow(@RequestHeader("x-auth-token") String token, @Valid @RequestBody BodyAddFlow data) {
        var user = this.userRepository.authenticateUser(token);

        Flow flow = new Flow();
        flow.setName(data.name);
        flow.setAuthorId(user.getId());
        flow.setDescription(data.description);

        Flow.objects.insert(flow);
        return new BasicFlowInfo(flow);
    }

    private record BodyUpdateFlow(
        @NotNull @Length(min = 1, max = 32) String name, String description) {
    }

    @PatchMapping("{flowId}")
    public BasicFlowInfo updateFlow(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @Valid @RequestBody BodyUpdateFlow data
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);

        Flow partial = new Flow();
        partial.setName(data.name);
        partial.setDescription(data.description);
        partial.setGraph(null);

        Flow.objects.update(flow.getId(), partial);
        var updated = Flow.objects.get(flow.getId());
        return new BasicFlowInfo(updated);
    }

    @DeleteMapping("{flowId}")
    public void deleteFlow(@RequestHeader("x-auth-token") String token, @PathVariable UUID flowId) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        Flow.objects.delete(flow.getId());
    }

    // Graph operations

    private record BodyAddNode(
        @JsonProperty("node_type") String nodeType
    ) {
    }

    @PostMapping(value = "{flowId}/graph", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addNode(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @Valid @RequestBody BodyAddNode body
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var nodeType = NodeType.REGISTRY.get(body.nodeType)
            .orElseThrow(InvalidNodeTypeException::new);
        var node = graph.addNode(nodeType);
        Flow.objects.updateGraph(flow);
        return SerializationUtils.toJson(node);
    }

    @PatchMapping("{flowId}/graph/nodes/{nodeId}")
    public void modifyNodeSettings(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId,
        @RequestBody String body
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var newSettings = SerializationUtils.readMap(body, Function.identity(), DataBox.class);
        for (var entry : newSettings.entrySet()) {
            var key = entry.getKey();
            DataBox<?> dataBox = entry.getValue();
            graph.modifyNode(nodeId, node -> node.modifySetting(key, dataBox));
        }
        Flow.objects.updateGraph(flow);
    }

    @DeleteMapping("{flowId}/graph/nodes/{nodeId}")
    public void deleteNode(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        graph.removeNode(nodeId);
        Flow.objects.updateGraph(flow);
    }

    private record BodyConnection(
        @JsonProperty("from_node_id") UUID fromNodeId,
        @JsonProperty("from_name") String fromName,
        @JsonProperty("to_node_id") UUID toNodeId,
        @JsonProperty("to_name") String toName,
        @JsonProperty("type") String type
    ) {
    }

    @PostMapping("{flowId}/graph/connections")
    public void addConnection(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @RequestBody BodyConnection body
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var connection = createConnection(graph, body);
        graph.addConnection(connection);
        Flow.objects.updateGraph(flow);
    }

    @DeleteMapping("{flowId}/graph/connections")
    public void deleteConnection(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @RequestBody BodyConnection body
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        var connection = createConnection(graph, body);
        graph.removeConnection(connection);
        Flow.objects.updateGraph(flow);
    }

    private static NodeConnection<?> createConnection(Graph graph, BodyConnection body) {
        var fromNode = graph.getNode(body.fromNodeId)
            .orElseThrow(() -> new NodeNotFoundException(body.fromNodeId));
        var toNode = graph.getNode(body.toNodeId)
            .orElseThrow(() -> new NodeNotFoundException(body.toNodeId));
        var type = DataType.REGISTRY.get(body.type)
            .orElseThrow(InvalidDataTypeException::new);
        return NodeConnection.createConnection(
            fromNode,
            body.fromName,
            toNode,
            body.toName,
            type
        ).orElseThrow(InvalidConnectionException::new);
    }

    @PatchMapping("{flowId}/graph/nodes/{nodeId}/metadata")
    public void modifyNodeMetadata(
        @RequestHeader("x-auth-token") String token,
        @PathVariable UUID flowId,
        @PathVariable UUID nodeId,
        @RequestBody NodeMetadata metadata
    ) {
        var user = this.userRepository.authenticateUser(token);
        var flow = this.flowRepository.getFlow(user, flowId);
        var graph = flow.getGraph();
        graph.setMetadata(nodeId, metadata);
        Flow.objects.updateGraph(flow);
    }
}
