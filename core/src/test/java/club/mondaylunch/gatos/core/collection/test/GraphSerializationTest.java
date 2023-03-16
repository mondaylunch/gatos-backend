package club.mondaylunch.gatos.core.collection.test;

import java.util.List;
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
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

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
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
    }

    @Test
    public void canSaveFlowWithNodes() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(3, graph.nodeCount());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        assertContainsNodes(graph, node1, node2, node3);
    }

    @Test
    public void canAddNodes() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        var node1 = retrievedGraph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = retrievedGraph.addNode(TestNodeTypes.PROCESS);
        var node3 = retrievedGraph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(3, finalGraph.nodeCount());
        assertContainsNodes(finalGraph, node1, node2, node3);
    }

    @Test
    public void canModifyNodes() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(3, graph.nodeCount());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        var modifiedNode1 = retrievedGraph.modifyNode(node1.id(), node -> node.modifySetting("setting", DataType.NUMBER.create(1.0)));
        var modifiedNode2 = retrievedGraph.modifyNode(node2.id(), node -> node.modifySetting("setting", DataType.NUMBER.listOf().create(List.of(1.0, 2.0))));
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(3, finalGraph.nodeCount());
        assertContainsNodes(finalGraph, modifiedNode1, modifiedNode2, node3);
    }

    @Test
    public void canRemoveNodes() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        Assertions.assertEquals(3, graph.nodeCount());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        assertContainsNodes(graph, node1, node2, node3);
        retrievedGraph.removeNode(node1.id());
        retrievedGraph.removeNode(node2.id());
        Flow.objects.updateGraph(retrievedFlow);
        Assertions.assertEquals(1, retrievedGraph.nodeCount());
        assertContainsNodes(retrievedGraph, node3);
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(1, finalGraph.nodeCount());
        assertContainsNodes(finalGraph, node3);
        assertDoesNotContainNodes(finalGraph, node1.id(), node2.id());
    }

    @Test
    public void canSaveFlowWithConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connection2 = NodeConnection.create(node2, "process_output", node3, "end_input");
        graph.addConnection(connection1);
        graph.addConnection(connection2);
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(2, graph.connectionCount());
        assertContainsNodes(graph, node1, node2, node3);
    }

    @Test
    public void canAddConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        var node1 = retrievedGraph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = retrievedGraph.addNode(TestNodeTypes.PROCESS);
        var node3 = retrievedGraph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connection2 = NodeConnection.create(node2, "process_output", node3, "end_input");
        retrievedGraph.addConnection(connection1);
        retrievedGraph.addConnection(connection2);
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        Assertions.assertEquals(2, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(3, finalGraph.nodeCount());
        Assertions.assertEquals(2, finalGraph.connectionCount());
        assertContainsNodes(finalGraph, node1, node2, node3);
        assertContainsConnections(finalGraph, connection1, connection2);
    }

    @Test
    public void canChangeConnectionThatChangesOtherConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());

        var node1 = retrievedGraph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = retrievedGraph.addNode(TestNodeTypes.TEST_VARYING_OUTPUT_NODE_TYPE);
        var node3 = retrievedGraph.addNode(TestNodeTypes.END_STRING);

        var connection1 = NodeConnection.create(node1, "start_output", node2, "in");
        retrievedGraph.addConnection(connection1);
        node2 = retrievedGraph.getNode(node2.id()).orElseThrow();
        var connection2 = NodeConnection.create(node2, "out", node3, "end_input");
        retrievedGraph.addConnection(connection2);
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        Assertions.assertEquals(2, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);

        var secondRetrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, secondRetrievedFlow);
        var secondRetrievedGraph = secondRetrievedFlow.getGraph();
        Assertions.assertEquals(3, secondRetrievedGraph.nodeCount());
        Assertions.assertEquals(2, secondRetrievedGraph.connectionCount());

        secondRetrievedGraph.removeConnection(secondRetrievedGraph.getConnection(node1.id(), "start_output", node2.id(), "in").orElseThrow());
        Assertions.assertEquals(1, secondRetrievedGraph.connectionCount());
        connection2 = secondRetrievedGraph.getConnectionsForNode(node2.id()).stream().findFirst().orElseThrow();
        Flow.objects.updateGraph(secondRetrievedFlow);
        assertFlowCount(1);

        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(secondRetrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(3, finalGraph.nodeCount());
        Assertions.assertEquals(1, finalGraph.connectionCount());
        assertContainsConnections(finalGraph, connection2);
    }

    @Test
    public void canChangeConnectionThatRemovesOtherConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(0, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());

        var node1 = retrievedGraph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = retrievedGraph.addNode(TestNodeTypes.TEST_VARYING_OUTPUT_NODE_TYPE);
        var node3 = retrievedGraph.addNode(TestNodeTypes.END);

        var connection1 = NodeConnection.create(node1, "start_output", node2, "in");
        retrievedGraph.addConnection(connection1);
        node2 = retrievedGraph.getNode(node2.id()).orElseThrow();
        var connection2 = NodeConnection.create(node2, "out", node3, "end_input");
        retrievedGraph.addConnection(connection2);
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        Assertions.assertEquals(2, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);

        var secondRetrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, secondRetrievedFlow);
        var secondRetrievedGraph = secondRetrievedFlow.getGraph();
        Assertions.assertEquals(3, secondRetrievedGraph.nodeCount());
        Assertions.assertEquals(2, secondRetrievedGraph.connectionCount());

        secondRetrievedGraph.removeConnection(secondRetrievedGraph.getConnection(node1.id(), "start_output", node2.id(), "in").orElseThrow());
        Assertions.assertEquals(0, secondRetrievedGraph.connectionCount());
        Flow.objects.updateGraph(secondRetrievedFlow);
        assertFlowCount(1);

        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(secondRetrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(3, finalGraph.nodeCount());
        Assertions.assertEquals(0, finalGraph.connectionCount());
    }

    @Test
    public void canRemoveConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.PROCESS);
        var node4 = graph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connection2 = NodeConnection.create(node2, "process_output", node3, "process_input");
        var connection3 = NodeConnection.create(node3, "process_output", node4, "end_input");
        graph.addConnection(connection1);
        graph.addConnection(connection2);
        graph.addConnection(connection3);
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(4, retrievedGraph.nodeCount());
        Assertions.assertEquals(3, retrievedGraph.connectionCount());
        assertContainsNodes(retrievedGraph, node1, node2, node3, node4);
        retrievedGraph.removeConnection(connection1);
        retrievedGraph.removeConnection(connection2);
        Assertions.assertEquals(1, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(1, finalGraph.connectionCount());
        var connections = finalGraph.getConnections();
        Assertions.assertEquals(1, connections.size());
        Assertions.assertTrue(connections.contains(connection3));
    }

    @Test
    public void canAddAndRemoveConnection() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.create(node1, "start_output", node2, "process_input");
        graph.addConnection(connection1);
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, retrievedGraph.nodeCount());
        Assertions.assertEquals(1, retrievedGraph.connectionCount());
        assertContainsNodes(retrievedGraph, node1, node2, node3);
        var connection2 = NodeConnection.create(node2, "process_output", node3, "end_input");
        retrievedGraph.addConnection(connection2);
        Assertions.assertEquals(2, retrievedGraph.connectionCount());
        retrievedGraph.removeConnection(connection1);
        Assertions.assertEquals(1, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(1, finalGraph.connectionCount());
        var connections = finalGraph.getConnections();
        Assertions.assertEquals(1, connections.size());
        Assertions.assertTrue(connections.contains(connection2));
    }

    @Test
    public void canSaveFlowWithMetaData() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        var metadata1 = graph.modifyMetadata(node1.id(), nodeMetadata -> nodeMetadata.withX(1));
        var metadata2 = graph.modifyMetadata(node2.id(), nodeMetadata -> nodeMetadata.withX(2));
        Assertions.assertEquals(1, metadata1.xPos());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        assertContainsNodes(graph, node1, node2, node3);
        Assertions.assertEquals(metadata1, retrievedGraph.getOrCreateMetadataForNode(node1.id()));
        Assertions.assertEquals(metadata2, retrievedGraph.getOrCreateMetadataForNode(node2.id()));
        NodeMetadata defaultMetadata = new NodeMetadata(0, 0);
        Assertions.assertEquals(defaultMetadata, retrievedGraph.getOrCreateMetadataForNode(node3.id()));
    }

    @Test
    public void canAddMetadata() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        UUID nodeId1 = UUID.randomUUID();
        UUID nodeId2 = UUID.randomUUID();
        var metadata1 = retrievedGraph.modifyMetadata(nodeId1, metadata -> metadata.withX(1));
        var metadata2 = retrievedGraph.modifyMetadata(nodeId2, metadata -> metadata.withX(2));
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(metadata1, finalGraph.getOrCreateMetadataForNode(nodeId1));
        Assertions.assertEquals(metadata2, finalGraph.getOrCreateMetadataForNode(nodeId2));
    }

    @Test
    public void canModifyMetadata() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var nodeId1 = UUID.randomUUID();
        var nodeId2 = UUID.randomUUID();
        graph.modifyMetadata(nodeId1, nodeMetadata -> nodeMetadata.withX(1));
        graph.modifyMetadata(nodeId2, nodeMetadata -> nodeMetadata.withX(2));
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        var updatedMetadata1 = retrievedGraph.modifyMetadata(nodeId1, nodeMetadata -> nodeMetadata.withY(1));
        var updatedMetadata2 = retrievedGraph.modifyMetadata(nodeId2, nodeMetadata -> nodeMetadata.withY(2));
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(updatedMetadata1, finalGraph.getOrCreateMetadataForNode(nodeId1));
        Assertions.assertEquals(updatedMetadata2, finalGraph.getOrCreateMetadataForNode(nodeId2));
    }

    @Test
    public void canRemoveNodesWithConnections() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.PROCESS);
        var node4 = graph.addNode(TestNodeTypes.END);
        var connection1 = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connection2 = NodeConnection.create(node2, "process_output", node3, "process_input");
        var connection3 = NodeConnection.create(node3, "process_output", node4, "end_input");
        graph.addConnection(connection1);
        graph.addConnection(connection2);
        graph.addConnection(connection3);
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(4, retrievedGraph.nodeCount());
        Assertions.assertEquals(3, retrievedGraph.connectionCount());
        assertContainsNodes(retrievedGraph, node1, node2, node3, node4);
        retrievedGraph.removeNode(node1.id());
        retrievedGraph.removeNode(node4.id());
        Assertions.assertEquals(2, retrievedGraph.nodeCount());
        Assertions.assertEquals(1, retrievedGraph.connectionCount());
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(2, finalGraph.nodeCount());
        assertContainsNodes(finalGraph, node2, node3);
        assertDoesNotContainNodes(finalGraph, node1.id(), node4.id());
        Assertions.assertEquals(1, finalGraph.connectionCount());
        var connections = finalGraph.getConnections();
        Assertions.assertEquals(1, connections.size());
        Assertions.assertTrue(connections.contains(connection2));
    }

    @Test
    public void canRemoveNodesWithMetadata() {
        var flow = createFlow();
        var id = flow.getId();
        var graph = flow.getGraph();
        var node1 = graph.addNode(TestNodeTypes.NO_INPUTS);
        var node2 = graph.addNode(TestNodeTypes.PROCESS);
        var node3 = graph.addNode(TestNodeTypes.END);
        var metadata1 = graph.modifyMetadata(node1.id(), nodeMetadata -> nodeMetadata.withX(1));
        var metadata2 = graph.modifyMetadata(node2.id(), nodeMetadata -> nodeMetadata.withX(2));
        var metadata3 = graph.modifyMetadata(node3.id(), nodeMetadata -> nodeMetadata.withX(3));
        Assertions.assertEquals(1, metadata1.xPos());
        Flow.objects.insert(flow);
        assertFlowCount(1);
        var retrievedFlow = Flow.objects.get(id);
        Assertions.assertEquals(flow, retrievedFlow);
        var retrievedGraph = retrievedFlow.getGraph();
        Assertions.assertEquals(graph, retrievedGraph);
        Assertions.assertEquals(3, graph.nodeCount());
        Assertions.assertEquals(0, graph.connectionCount());
        assertContainsNodes(graph, node1, node2, node3);
        Assertions.assertEquals(metadata1, retrievedGraph.getOrCreateMetadataForNode(node1.id()));
        Assertions.assertEquals(metadata2, retrievedGraph.getOrCreateMetadataForNode(node2.id()));
        Assertions.assertEquals(metadata3, retrievedGraph.getOrCreateMetadataForNode(node3.id()));
        retrievedGraph.removeNode(node1.id());
        retrievedGraph.removeNode(node2.id());
        Assertions.assertEquals(1, retrievedGraph.nodeCount());
        var defaultMetadata = new NodeMetadata(0, 0);
        Flow.objects.updateGraph(retrievedFlow);
        assertFlowCount(1);
        var finalFlow = Flow.objects.get(id);
        Assertions.assertEquals(retrievedFlow, finalFlow);
        /*
        This has to be tested after the flow equality as
        getOrCreateMetadataForNode will create a new
        metadata object, causing the two flows to be
        not equal.
         */
        Assertions.assertEquals(defaultMetadata, retrievedGraph.getOrCreateMetadataForNode(node1.id()));
        Assertions.assertEquals(defaultMetadata, retrievedGraph.getOrCreateMetadataForNode(node2.id()));
        Assertions.assertEquals(metadata3, retrievedGraph.getOrCreateMetadataForNode(node3.id()));
        var finalGraph = finalFlow.getGraph();
        Assertions.assertEquals(1, finalGraph.nodeCount());
        assertContainsNodes(finalGraph, node3);
        assertDoesNotContainNodes(finalGraph, node1.id(), node2.id());
        Assertions.assertEquals(defaultMetadata, finalGraph.getOrCreateMetadataForNode(node1.id()));
        Assertions.assertEquals(defaultMetadata, finalGraph.getOrCreateMetadataForNode(node2.id()));
        Assertions.assertEquals(metadata3, finalGraph.getOrCreateMetadataForNode(node3.id()));
    }

    private static Flow createFlow() {
        return new Flow(UUID.randomUUID(), "Test Flow", UUID.randomUUID());
    }

    private static void assertFlowCount(long expected) {
        Assertions.assertEquals(expected, Flow.objects.size());
    }

    private static void assertContainsNodes(Graph graph, Node... nodes) {
        for (var node : nodes) {
            Assertions.assertTrue(graph.containsNode(node));
        }
    }

    private static void assertDoesNotContainNodes(Graph graph, UUID... nodeIds) {
        for (var nodeId : nodeIds) {
            Assertions.assertFalse(graph.containsNode(nodeId));
        }
    }

    private static void assertContainsConnections(Graph graph, NodeConnection<?>... connections) {
        var graphConnections = graph.getConnections();
        Assertions.assertTrue(graphConnections.containsAll(List.of(connections)));
    }
}
