package club.mondaylunch.gatos.core.collection.test;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.test.TestNodeTypes;
import club.mondaylunch.gatos.core.models.Flow;

public class GraphSerializationTest {

    @BeforeEach
    void setUp() {
        this.reset();
        assertFlowCount(0);
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        Flow.objects.clear();
    }

    @Test
    public void canSaveFlowWithEmptyGraph() {
        Flow flow = createFlow();
        UUID id = flow.getId();
        Graph graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        Flow retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        Graph retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
    }

    @Test
    public void canSaveFlowWithNodes() {
        Flow flow = createFlow();
        UUID id = flow.getId();
        Graph graph = flow.getGraph();
        Node node1 = graph.addNode(TestNodeTypes.START);
        Node node2 = graph.addNode(TestNodeTypes.PROCESS);
        Node node3 = graph.addNode(TestNodeTypes.END);
        Flow.objects.insert(flow);
        assertFlowCount(1);
        Flow retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        Graph retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        Assertions.assertTrue(graph.containsNode(node1));
        Assertions.assertTrue(graph.containsNode(node2));
        Assertions.assertTrue(graph.containsNode(node3));
    }

    @Test
    public void canSaveFlowWithConnections() {
        Flow flow = createFlow();
        UUID id = flow.getId();
        Graph graph = flow.getGraph();
        Node node1 = graph.addNode(TestNodeTypes.START);
        Node node2 = graph.addNode(TestNodeTypes.PROCESS);
        Node node3 = graph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.createConnection(node1, "start_output", node2, "process_input", DataType.NUMBER);
        var connection2 = NodeConnection.createConnection(node2, "process_output", node3, "end_input", DataType.NUMBER);
        graph.addConnection(connection1.orElseThrow());
        graph.addConnection(connection2.orElseThrow());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        Flow retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        Graph retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(2, graph.connectionCount());
        Assertions.assertTrue(graph.containsNode(node1));
        Assertions.assertTrue(graph.containsNode(node2));
        Assertions.assertTrue(graph.containsNode(node3));
    }

    @Test
    public void canSaveFlowWithMetaData() {
        Flow flow = createFlow();
        UUID id = flow.getId();
        Graph graph = flow.getGraph();
        Node node1 = graph.addNode(TestNodeTypes.START);
        Node node2 = graph.addNode(TestNodeTypes.PROCESS);
        Node node3 = graph.addNode(TestNodeTypes.END);
        NodeMetadata metadata = graph.modifyMetadata(node1.id(), nodeMetadata -> nodeMetadata.withX(1));
        Assertions.assertEquals(1, metadata.xPos());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        Flow retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        Graph retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        Assertions.assertTrue(graph.containsNode(node1));
        Assertions.assertTrue(graph.containsNode(node2));
        Assertions.assertTrue(graph.containsNode(node3));
        Assertions.assertEquals(metadata, retrievedGraph.getOrCreateMetadataForNode(node1.id()));
        NodeMetadata defaultMetadata = new NodeMetadata(0, 0);
        Assertions.assertEquals(defaultMetadata, retrievedGraph.getOrCreateMetadataForNode(node2.id()));
        Assertions.assertEquals(defaultMetadata, retrievedGraph.getOrCreateMetadataForNode(node3.id()));
    }

    private static Flow createFlow() {
        return new Flow(UUID.randomUUID(), "Test Flow", UUID.randomUUID());
    }

    private static void assertFlowCount(long expected) {
        Assertions.assertEquals(expected, Flow.objects.size());
    }
}
