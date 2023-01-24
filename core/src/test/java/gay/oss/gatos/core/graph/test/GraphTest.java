package gay.oss.gatos.core.graph.test;

import gay.oss.gatos.core.graph.Graph;
import gay.oss.gatos.core.graph.NodeMetadata;
import gay.oss.gatos.core.graph.NodeType;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GraphTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void canRemoveNodeFromGraph() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        graph.removeNode(node.id());
        Assertions.assertFalse(graph.containsNode(node));
        Assertions.assertFalse(graph.containsNode(node.id()));
    }

    @Test
    public void canModifyNode() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        var modifiedNode = graph.modifyNode(node.id(), n -> n.modifySetting("setting_1", 100));
        Assertions.assertFalse(graph.containsNode(node));
        Assertions.assertTrue(graph.containsNode(node.id()));
        Assertions.assertTrue(graph.containsNode(modifiedNode));
        Assertions.assertEquals(100, modifiedNode.getSetting("setting_1", Integer.class).value());
    }

    @Test
    public void canAddConnection() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "in", node2, "out", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());

        graph.addConnection(conn.get());

        Assertions.assertTrue(graph.getConnectionsForNode(node1.id()).contains(conn.get()));
        Assertions.assertTrue(graph.getConnectionsForNode(node2.id()).contains(conn.get()));
    }

    @Test
    public void canRemoveConnection() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "in", node2, "out", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());

        graph.addConnection(conn.get());
        graph.removeConnection(conn.get());

        Assertions.assertFalse(graph.getConnectionsForNode(node1.id()).contains(conn.get()));
        Assertions.assertFalse(graph.getConnectionsForNode(node2.id()).contains(conn.get()));
    }

    @Test
    public void canAccessMetadata() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        var metadata = graph.getOrCreateMetadataForNode(node.id());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(new NodeMetadata(0f, 0f), metadata);
    }


    @Test
    public void canModifyMetadata() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        var res = graph.modifyMetadata(node.id(), meta -> meta.withX(10f).withY(20f));
        Assertions.assertEquals(new NodeMetadata(10f, 20f), res);
        Assertions.assertEquals(new NodeMetadata(10f, 20f), graph.getOrCreateMetadataForNode(node.id()));
    }

    private static final class TestNodeType implements NodeType {
        @Override
        public boolean hasInputs() {
            return true;
        }

        @Override
        public boolean hasOutputs() {
            return true;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                    new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                    "setting_1", DataType.INTEGER.create(0)
            );
        }
    }
}
