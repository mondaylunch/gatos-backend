package club.mondaylunch.gatos.api.controller.test;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.TestSecurity;
import club.mondaylunch.gatos.api.controller.FlowController;
import club.mondaylunch.gatos.api.helpers.UserCreationHelper;
import club.mondaylunch.gatos.core.GatosCore;
import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphObserver;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

@SpringBootTest
@AutoConfigureMockMvc
public class FlowControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/flows";
    private final User user = this.createRandomUser();

    @BeforeAll
    public static void init() {
        GatosCore.init();
    }

    @BeforeEach
    public void setupMockJwt() {
        Mockito.when(this.decoder.decode(anyString())).thenReturn(TestSecurity.jwt());
    }

    @AfterAll
    static void cleanUp() {
        Flow.objects.clear();
    }

    /// --- LIST FLOWS ---

    @Test
    public void canGetNoFlows() throws Exception {
        this.testGetFlows(0);
    }

    @Test
    public void canGetOneFlow() throws Exception {
        this.testGetFlows(1);
    }

    @Test
    public void canGetManyFlows() throws Exception {
        this.testGetFlows(10);
    }

    @Test
    public void cannotGetFlowsWithoutEmail() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void canGetSpecificFlow() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();

        var start = graph.addNode(TestNodeTypes.NO_INPUTS);
        var process = graph.addNode(TestNodeTypes.PROCESS);
        var end = graph.addNode(TestNodeTypes.END);

        var startToProcess = NodeConnection.create(start, "start_output", process, "process_input");
        var processToEnd = NodeConnection.create(process, "process_output", end, "end_input");
        graph.addConnection(startToProcess);
        graph.addConnection(processToEnd);

        graph.modifyMetadata(start.id(), nodeMetadata -> nodeMetadata.withX(1));

        Flow.objects.insert(flow);
        ResultActions result = this.mockMvc
            .perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId())
                .header("x-user-email", this.user.getEmail())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        compareFields(OBJECT_EXPRESSION_PREFIX, result,
            Map.entry("name", flow.getName()),
            Map.entry("author_id", flow.getAuthorId())
        );

        var nodesField = Graph.class.getDeclaredField("nodes");
        nodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var nodes = (Map<UUID, Node>) nodesField.get(graph);
        compareNodes(nodes.values(), result);
        compareConnections(graph.getConnections(), result);
        var metaDataField = Graph.class.getDeclaredField("metadataByNode");
        metaDataField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var metadataByNode = (Map<UUID, NodeMetadata>) metaDataField.get(graph);
        compareMetadataByNode(metadataByNode, result);
    }

    /// --- ADD FLOW ---

    @Test
    public void canAddFlow() throws Exception {
        Flow flow = new Flow();
        flow.setName("My Flow");
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(1);
        compareFields(OBJECT_EXPRESSION_PREFIX, result,
            Map.entry("name", flow.getName()),
            Map.entry("author_id", this.user.getId()));
    }

    @Test
    public void cannotAddFlowWithoutBody() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithInvalidBody() throws Exception {
        String invalidJson = "invalid";

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail())
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithoutEmail() throws Exception {
        Flow flow = createFlow(new User());
        String flowJson = MAPPER.writeValueAsString(flow);
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithoutContentType() throws Exception {
        Flow flow = createFlow(this.user);
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail())
            .content(flowJson));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(0);
    }

    /// --- UPDATE FLOW ---

    @Test
    public void cannotUpdateNoFlowFields() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail())
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void canUpdateFlowName() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(1);
        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
            Map.entry("name", update.getName()),
            Map.entry("author_id", flow.getAuthorId()));
        Flow newFlow = getFlow(result);
        Assertions.assertNotNull(newFlow);
        Assertions.assertEquals(update.getName(), newFlow.getName());
        Assertions.assertEquals(flow.getAuthorId(), newFlow.getAuthorId());
    }

    @Test
    public void cannotUpdateFlowAuthorId() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setAuthorId(UUID.randomUUID());
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail())
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutEmail() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutContentType() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail())
            .content(flowJson));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    /// --- DELETE FLOW ---

    @Test
    public void canDeleteFlow() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(0);
        Assertions.assertNull(Flow.objects.get(flow.getId()));
    }

    @Test
    public void cannotDeleteNonExistentFlow() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + UUID.randomUUID())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
            .header("x-user-email", this.user.getEmail()));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotDeleteFlowWithoutEmail() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Assertions.assertNotNull(Flow.objects.get(flow.getId()));
    }

    // Graph operations

    @Test
    public void canGetGraphNode() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        Assertions.assertEquals(0, node.getSetting("setting", DataType.NUMBER).value());
        Assertions.assertEquals(1, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId() + "/nodes/" + node.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var responseNode = SerializationUtils.fromJson(responseBody, Node.class);
        Assertions.assertEquals(node, responseNode);
    }

    @Test
    public void canAddGraphNode() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        Assertions.assertEquals(0, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var nodeType = "string_interpolation";
        var nodeTypeJson = new JsonObject();
        nodeTypeJson.addProperty("type", nodeType);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + flow.getId() + "/nodes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nodeTypeJson.toString()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var id = flow.getId();
        var updatedFlow = Flow.objects.get(id);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(1, updatedGraph.nodeCount());
        var body = result.andReturn().getResponse().getContentAsString();
        var changes = SerializationUtils.fromJson(body, GraphObserver.GraphChanges.class);
        var responseNode = changes.addedNodes().stream().filter(n -> n.type().equals(NodeType.REGISTRY.get(nodeType).orElseThrow())).findFirst();
        Assertions.assertTrue(responseNode.isPresent());
        var expectedNode = updatedGraph.getNode(responseNode.get().id()).orElseThrow();
        Assertions.assertEquals(expectedNode, responseNode.get());
    }

    @Test
    public void canModifyNodeSettings() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        Assertions.assertEquals(0, node.getSetting("setting", DataType.NUMBER).value());
        Assertions.assertEquals(1, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var flowId = flow.getId();
        var nodeId = node.id();
        var dataBox = new JsonObject();
        dataBox.addProperty("type", "number");
        dataBox.addProperty("value", 1);
        var body = new JsonObject();
        body.add("setting", dataBox);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId() + "/nodes/" + nodeId + "/settings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(flowId);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(1, updatedGraph.nodeCount());
        var updatedNode = updatedGraph.getNode(nodeId).orElseThrow();
        Assertions.assertEquals(1, updatedNode.getSetting("setting", DataType.NUMBER).value());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var changes = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
        Assertions.assertTrue(changes.addedNodes().contains(updatedNode));
    }

    @Test
    public void canDeleteGraphNode() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        Assertions.assertEquals(1, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var flowId = flow.getId();
        var nodeId = node.id();
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flowId + "/nodes/" + nodeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(flowId);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(0, updatedGraph.nodeCount());
        Assertions.assertFalse(updatedGraph.containsNode(nodeId));
    }

    @Test
    public void canGetConnections() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var start = graph.addNode(TestNodeTypes.NO_INPUTS);
        var end = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(2, graph.nodeCount());
        var connection = NodeConnection.create(start, "start_output", end, "end_input");
        graph.addConnection(connection);
        Assertions.assertEquals(1, graph.connectionCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var startConnectionsResult = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId() + "/connections/" + start.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var endConnectionsResult = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId() + "/connections/" + end.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var startConnectionsBody = startConnectionsResult.andReturn().getResponse().getContentAsString();
        var endConnectionsBody = endConnectionsResult.andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(startConnectionsBody, endConnectionsBody, JSONCompareMode.NON_EXTENSIBLE);
        var connectionsJson = JsonParser.parseString(startConnectionsBody).getAsJsonArray();
        Assertions.assertEquals(1, connectionsJson.size());
        var connectionJson = connectionsJson.get(0);
        var responseConnection = SerializationUtils.fromJson(connectionJson.toString(), NodeConnection.class);
        Assertions.assertEquals(connection, responseConnection);
    }

    @Test
    public void canAddConnection() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var start = graph.addNode(TestNodeTypes.NO_INPUTS);
        var end = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(2, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var body = new JsonObject();
        body.addProperty("from_node_id", start.id().toString());
        body.addProperty("from_name", "start_output");
        body.addProperty("to_node_id", end.id().toString());
        body.addProperty("to_name", "end_input");
        var result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + flow.getId() + "/connections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var id = flow.getId();
        var updatedFlow = Flow.objects.get(id);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(2, updatedGraph.nodeCount());
        Assertions.assertEquals(1, updatedGraph.connectionCount());
        var startConnections = updatedGraph.getConnectionsForNode(start.id());
        var endConnections = updatedGraph.getConnectionsForNode(end.id());
        Assertions.assertEquals(1, startConnections.size());
        Assertions.assertEquals(startConnections, endConnections);
        var expectedConnection = NodeConnection.create(start, "start_output", end, "end_input");
        Assertions.assertEquals(expectedConnection, startConnections.iterator().next());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var responseConnection = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
        Assertions.assertTrue(responseConnection.addedConnections().contains(expectedConnection));
    }

    @Test
    public void canRemoveConnection() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var start = graph.addNode(TestNodeTypes.NO_INPUTS);
        var end = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(2, graph.nodeCount());
        var connection = NodeConnection.create(start, "start_output", end, "end_input");
        graph.addConnection(connection);
        Assertions.assertEquals(1, graph.connectionCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var body = new JsonObject();
        body.addProperty("from_node_id", start.id().toString());
        body.addProperty("from_name", "start_output");
        body.addProperty("to_node_id", end.id().toString());
        body.addProperty("to_name", "end_input");
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId() + "/connections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var id = flow.getId();
        var updatedFlow = Flow.objects.get(id);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(2, updatedGraph.nodeCount());
        Assertions.assertEquals(0, updatedGraph.connectionCount());
        var startConnections = updatedGraph.getConnectionsForNode(start.id());
        var endConnections = updatedGraph.getConnectionsForNode(end.id());
        Assertions.assertEquals(0, startConnections.size());
        Assertions.assertEquals(startConnections, endConnections);
    }

    @Test
    public void canRemoveNodeWithConnections() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.MULTIPLE_CONNECTIONS);
        var node2 = graph.addNode(TestNodeTypes.MULTIPLE_CONNECTIONS);
        var node3 = graph.addNode(TestNodeTypes.MULTIPLE_CONNECTIONS);
        var node4 = graph.addNode(TestNodeTypes.MULTIPLE_CONNECTIONS);
        Assertions.assertEquals(4, graph.nodeCount());
        var connection1 = NodeConnection.create(node1, "output_1", node2, "input_1");
        var connection2 = NodeConnection.create(node1, "output_2", node2, "input_2");
        var connection3 = NodeConnection.create(node1, "output_3", node3, "input_1");
        var connection4 = NodeConnection.create(node3, "output_1", node4, "input_1");
        var connection5 = NodeConnection.create(node3, "output_2", node4, "input_2");
        var connection6 = NodeConnection.create(node3, "output_3", node4, "input_3");
        graph.addConnection(connection1);
        graph.addConnection(connection2);
        graph.addConnection(connection3);
        graph.addConnection(connection4);
        graph.addConnection(connection5);
        graph.addConnection(connection6);
        Assertions.assertEquals(6, graph.connectionCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var id = flow.getId();
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + id + "/nodes/" + node3.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(id);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(3, updatedGraph.nodeCount());
        Assertions.assertEquals(2, updatedGraph.connectionCount());
        var connections = updatedGraph.getConnections();
        Assertions.assertTrue(connections.contains(connection1));
        Assertions.assertTrue(connections.contains(connection2));
    }

    @Test
    public void canGetNodeMetadata() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        var flowId = flow.getId();
        var nodeId = node.id();
        Assertions.assertEquals(1, graph.nodeCount());
        var metadata = new NodeMetadata(1, 1);
        graph.setMetadata(nodeId, metadata);
        var actualMetadata = graph.getOrCreateMetadataForNode(node.id());
        Assertions.assertEquals(metadata, actualMetadata);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flowId + "/nodes/" + nodeId + "/metadata")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var responseMetadata = SerializationUtils.fromJson(responseBody, NodeMetadata.class);
        Assertions.assertEquals(metadata, responseMetadata);
    }

    @Test
    public void canModifyNodeMetadata() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        Assertions.assertEquals(1, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var expectedMetadata = new NodeMetadata(0, 0);
        var actualMetadata = graph.getOrCreateMetadataForNode(node.id());
        Assertions.assertEquals(expectedMetadata, actualMetadata);
        var flowId = flow.getId();
        var nodeId = node.id();
        var body = new JsonObject();
        body.addProperty("x_pos", 1);
        body.addProperty("y_pos", 1);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flowId + "/nodes/" + nodeId + "/metadata")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(flowId);
        var updatedGraph = updatedFlow.getGraph();
        expectedMetadata = new NodeMetadata(1, 1);
        actualMetadata = updatedGraph.getOrCreateMetadataForNode(nodeId);
        Assertions.assertEquals(expectedMetadata, actualMetadata);
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var changes = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
        Assertions.assertTrue(changes.addedMetadata().containsValue(expectedMetadata));
    }

    @Test
    public void canModifyExistingMetadata() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        var flowId = flow.getId();
        var nodeId = node.id();
        Assertions.assertEquals(1, graph.nodeCount());
        var metadata = new NodeMetadata(1, 1);
        graph.setMetadata(nodeId, metadata);
        var actualMetadata = graph.getOrCreateMetadataForNode(node.id());
        Assertions.assertEquals(metadata, actualMetadata);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        var body = new JsonObject();
        body.addProperty("x_pos", 2);
        body.addProperty("y_pos", 2);
        var result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flowId + "/nodes/" + nodeId + "/metadata")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(flowId);
        var updatedGraph = updatedFlow.getGraph();
        var expectedMetadata = new NodeMetadata(2, 2);
        actualMetadata = updatedGraph.getOrCreateMetadataForNode(nodeId);
        Assertions.assertEquals(expectedMetadata, actualMetadata);
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var responseMetadata = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
        Assertions.assertTrue(responseMetadata.addedMetadata().containsValue(expectedMetadata));
    }

    @Test
    public void canDeleteNodeWithMetaData() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        var node = graph.addNode(TestNodeTypes.NO_INPUTS);
        var flowId = flow.getId();
        var nodeId = node.id();
        var updatedMetadata = new NodeMetadata(1, 1);
        graph.setMetadata(nodeId, updatedMetadata);
        Assertions.assertEquals(updatedMetadata, graph.getOrCreateMetadataForNode(nodeId));
        Assertions.assertEquals(1, graph.nodeCount());
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flowId + "/nodes/" + nodeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var updatedFlow = Flow.objects.get(flowId);
        var updatedGraph = updatedFlow.getGraph();
        Assertions.assertEquals(0, updatedGraph.nodeCount());
        Assertions.assertFalse(updatedGraph.containsNode(nodeId));
        Assertions.assertEquals(new NodeMetadata(0, 0), updatedGraph.getOrCreateMetadataForNode(nodeId));
    }

    @Test
    public void canExecuteFlow() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        Assertions.assertEquals(0, graph.nodeCount());
        Flow.objects.insert(flow);
        var start = this.addNode(flow.getId(), "webhook_start");
        var end = this.addNode(flow.getId(), "webhook_end");
        this.addConnection(flow.getId(), start.id(), "requestBody", end.id(), "graphOutput");
        this.addConnection(flow.getId(), start.id(), "endOutputReference", end.id(), "outputReference");
        var inputBody = new JsonObject();
        inputBody.addProperty("input", "value");
        var inputBodyString = inputBody.toString();
        var result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + flow.getId() + "/run/" + start.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(inputBodyString)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(inputBodyString, responseBody, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void canGetErrors() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();
        Assertions.assertEquals(0, graph.nodeCount());
        Flow.objects.insert(flow);
        this.addNode(flow.getId(), "webhook_start");
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId() + "/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        var responseBody = result.andReturn().getResponse().getContentAsString();
        var responseErrors = SerializationUtils.fromJson(responseBody, FlowController.GraphErrorInfo.class);
        Assertions.assertFalse(responseErrors.errors().isEmpty());
    }

    private Node addNode(UUID flowId, String nodeType) {
        var requestBody = new JsonObject();
        requestBody.addProperty("type", nodeType);
        var nodeCountBefore = getNodeCount(flowId);
        try {
            var result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + flowId + "/nodes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                    .header("x-user-email", this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody.toString())
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
            Assertions.assertEquals(nodeCountBefore + 1, getNodeCount(flowId));
            var responseBody = result.andReturn().getResponse().getContentAsString();
            var responseChanges = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
            var responseNode = responseChanges.addedNodes().stream().filter(n -> n.type().equals(NodeType.REGISTRY.get(nodeType).orElseThrow())).findFirst();
            Assertions.assertTrue(responseNode.isPresent());
            return responseNode.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private NodeConnection<?> addConnection(UUID flowId, UUID fromNodeId, String fromName, UUID toNodeId, String toName) {
        var requestBody = new JsonObject();
        requestBody.addProperty("from_node_id", fromNodeId.toString());
        requestBody.addProperty("from_name", fromName);
        requestBody.addProperty("to_node_id", toNodeId.toString());
        requestBody.addProperty("to_name", toName);
        var connectionCountBefore = getConnectionCount(flowId);
        try {
            var result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + flowId + "/connections")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                    .header("x-user-email", this.user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody.toString())
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
            Assertions.assertEquals(connectionCountBefore + 1, getConnectionCount(flowId));
            var responseBody = result.andReturn().getResponse().getContentAsString();
            var changes = SerializationUtils.fromJson(responseBody, GraphObserver.GraphChanges.class);
            Optional<NodeConnection<?>> responseConnection = changes.addedConnections().stream()
                .filter(conn -> conn.from().nodeId().equals(fromNodeId)
                    && conn.from().name().equals(fromName)
                    && conn.to().nodeId().equals(toNodeId)
                    && conn.to().name().equals(toName)
                ).findAny();
            Assertions.assertTrue(responseConnection.isPresent());
            return responseConnection.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getNodeCount(UUID flowId) {
        return Flow.objects.get(flowId).getGraph().nodeCount();
    }

    private static int getConnectionCount(UUID flowId) {
        return Flow.objects.get(flowId).getGraph().connectionCount();
    }

    /// --- UTILITIES ---

    private void assertFlowCount(long count) {
        Assertions.assertEquals(count, this.getFlowCount());
    }

    private long getFlowCount() {
        return Flow.objects.countByUserId(this.user.getId());
    }

    private static Flow createFlow(User author) {
        return new Flow(UUID.randomUUID(), "Test Flow", author.getId());
    }

    private static Flow getFlow(ResultActions result) throws Exception {
        String responseJson = result.andReturn().getResponse().getContentAsString();
        UUID flowId = UUID.fromString(JsonPath.read(responseJson, "$._id"));
        return Flow.objects.get(flowId);
    }

    private void testGetFlows(int flowCount) throws Exception {
        List<Flow> flows = new ArrayList<>();
        for (int i = 0; i < flowCount; i++) {
            Flow flow = new Flow(UUID.randomUUID(), "Test Flow " + i, this.user.getId());
            Flow.objects.insert(flow);
            flows.add(flow);
        }
        ResultActions result = this.getFlows(flowCount);
        for (int i = 0; i < flowCount; i++) {
            result = compareFlow(flows.get(i), i, result);
        }
    }

    private ResultActions getFlows(int expectedFlowCount) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", this.user.getEmail()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$",
                Matchers.hasSize(expectedFlowCount)));
    }

    private static void assertFlowEquality(Flow oldFlow, Flow newFlow) {
        Assertions.assertNotNull(oldFlow);
        Assertions.assertNotNull(newFlow);
        Assertions.assertEquals(oldFlow.getName(), newFlow.getName());
        Assertions.assertEquals(oldFlow.getAuthorId(), newFlow.getAuthorId());
    }

    private static ResultActions compareFlow(Flow flow, int index, ResultActions result) {
        return compareFields(objectArrayExpressionPrefix(index), result,
            Map.entry("name", flow.getName()),
            Map.entry("author_id", flow.getAuthorId()));
    }

    private static <T> void compareUnordered(
        Collection<T> elements,
        ResultActions result,
        UnorderedComparator<T> comparator,
        String ignoredErrorMessageStart
    ) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            indexes.add(i);
        }
        for (var element : elements) {
            var indexIterator = indexes.listIterator();
            while (indexIterator.hasNext()) {
                var index = indexIterator.next();
                try {
                    comparator.compare(element, index, result);
                    indexIterator.remove();
                    break;
                } catch (AssertionError e) {
                    if (!e.getMessage().startsWith(ignoredErrorMessageStart)) {
                        throw e;
                    }
                }
            }
        }
        if (!indexes.isEmpty()) {
            throw new AssertionError("Could not find elements with indexes " + indexes);
        }
    }

    private static void compareNodes(Collection<Node> nodes, ResultActions result) {
        compareUnordered(
            nodes,
            result,
            FlowControllerTest::compareNode,
            "JSON path \"$.graph.nodes["
        );
    }

    private static void compareNode(Node node, int index, ResultActions result) {
        var prefix = OBJECT_EXPRESSION_PREFIX + "graph.nodes[" + index + "].";
        compareNode(node, result, prefix);
    }

    private static void compareNode(Node node, ResultActions result, String prefix) {
        compareFields(prefix, result,
            Map.entry("id", node.id()),
            Map.entry("type", NodeType.REGISTRY.getName(node.type()).orElseThrow())
        );
        var settingsPrefix = prefix + "settings.";
        for (var setting : node.settings().entrySet()) {
            var name = setting.getKey();
            var dataBox = setting.getValue();
            var type = dataBox.type();
            var value = dataBox.value();
            compareFields(settingsPrefix + name + '.', result,
                Map.entry("type", DataType.REGISTRY.getName(type).orElseThrow())
            );
            if (value instanceof Optional<?> optional) {
                compareFields(settingsPrefix + name + ".value.", result,
                    Map.entry("present", optional.isPresent())
                );
                optional.ifPresent(optionalValue ->
                    compareFields(settingsPrefix + name + ".value.", result,
                        Map.entry("value", optionalValue)
                    )
                );
            } else {
                compareFields(settingsPrefix + name + '.', result,
                    Map.entry("value", value)
                );
            }
        }
    }

    private static void compareConnections(Collection<NodeConnection<?>> connections, ResultActions result) {
        compareUnordered(
            connections,
            result,
            FlowControllerTest::compareConnection,
            "JSON path \"$.graph.connections["
        );
    }

    private static void compareConnection(NodeConnection<?> connection, int index, ResultActions result) {
        var prefix = OBJECT_EXPRESSION_PREFIX + "graph.connections[" + index + "].";
        compareConnector(connection.from(), prefix, result);
        compareConnector(connection.to(), prefix, result);
    }

    private static void compareConnector(NodeConnector<?> connector, String expressionPrefix, ResultActions result) {
        Class<?> connectorClass = connector.getClass();
        String type;
        if (connectorClass == NodeConnector.Input.class) {
            type = "input";
        } else if (connectorClass == NodeConnector.Output.class) {
            type = "output";
        } else {
            throw new IllegalArgumentException("Unknown connector class " + connectorClass);
        }
        compareFields(expressionPrefix + type + '.', result,
            Map.entry("node_id", connector.nodeId()),
            Map.entry("name", connector.name()),
            Map.entry("type", connector.type().name())
        );
    }

    private static void compareMetadataByNode(Map<UUID, NodeMetadata> metadataByNode, ResultActions result) {
        var prefix = OBJECT_EXPRESSION_PREFIX + "graph.metadata.";
        for (var metaData : metadataByNode.entrySet()) {
            compareMetadata(metaData.getKey(), metaData.getValue(), prefix, result);
        }
    }

    private static void compareMetadata(UUID nodeId, NodeMetadata metadata, String expressionPrefix, ResultActions result) {
        compareFields(expressionPrefix + nodeId + '.', result,
            Map.entry("x_pos", (double) metadata.xPos()),
            Map.entry("y_pos", (double) metadata.yPos())
        );
    }

    @FunctionalInterface
    private interface UnorderedComparator<T> {
        void compare(T expected, int index, ResultActions result);
    }
}
