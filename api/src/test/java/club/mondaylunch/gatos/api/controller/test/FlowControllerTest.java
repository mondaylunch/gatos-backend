package club.mondaylunch.gatos.api.controller.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.helpers.UserCreationHelper;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
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
    private User user;

    @BeforeEach
    void setUp() {
        this.user = this.createRandomUser();
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
    public void cannotGetFlowsWithoutToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void cannotGetFlowsWithInvalidToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list")
                .header("x-auth-token", "invalid"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void canGetSpecificFlow() throws Exception {
        var flow = createFlow(this.user);
        var graph = flow.getGraph();

        var start = graph.addNode(TestNodeTypes.START);
        var process = graph.addNode(TestNodeTypes.PROCESS);
        var end = graph.addNode(TestNodeTypes.END);

        var startToProcess = NodeConnection.createConnection(start, "start_output", process, "process_input", DataType.NUMBER);
        var processToEnd = NodeConnection.createConnection(process, "process_output", end, "end_input", DataType.NUMBER);
        graph.addConnection(startToProcess.orElseThrow());
        graph.addConnection(processToEnd.orElseThrow());

        graph.modifyMetadata(start.id(), nodeMetadata -> nodeMetadata.withX(1));

        Flow.objects.insert(flow);
        ResultActions result = this.mockMvc
            .perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken())
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
                .header("x-auth-token", this.user.getAuthToken())
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
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithInvalidBody() throws Exception {
        String invalidJson = "invalid";

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .header("x-auth-token", this.user.getAuthToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithoutToken() throws Exception {
        Flow flow = createFlow(new User());
        String flowJson = MAPPER.writeValueAsString(flow);
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
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
            .header("x-auth-token", this.user.getAuthToken())
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
            .header("x-auth-token", this.user.getAuthToken())
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
                .header("x-auth-token", this.user.getAuthToken())
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
            .header("x-auth-token", this.user.getAuthToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutToken() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
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
            .header("x-auth-token", this.user.getAuthToken())
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
                .header("x-auth-token", this.user.getAuthToken()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(0);
        Assertions.assertNull(Flow.objects.get(flow.getId()));
    }

    @Test
    public void cannotDeleteNonExistentFlow() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + UUID.randomUUID())
            .header("x-auth-token", this.user.getAuthToken()));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotDeleteFlowWithoutToken() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId()));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Assertions.assertNotNull(Flow.objects.get(flow.getId()));
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
        return this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list")
                .header("x-auth-token", this.user.getAuthToken()))
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
            Map.entry("nodeId", connector.nodeId()),
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
            Map.entry("xPos", (double) metadata.xPos()),
            Map.entry("yPos", (double) metadata.yPos())
        );
    }

    @FunctionalInterface
    private interface UnorderedComparator<T> {
        void compare(T expected, int index, ResultActions result);
    }
}
