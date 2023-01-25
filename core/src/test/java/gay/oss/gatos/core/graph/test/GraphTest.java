package gay.oss.gatos.core.graph.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.graph.Graph;
import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.NodeCategory;
import gay.oss.gatos.core.graph.NodeMetadata;
import gay.oss.gatos.core.graph.NodeType;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;

public class GraphTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();
    private static final NodeType INPUT_NODE_TYPE = new TestInputNodeType();
    private static final NodeType OUTPUT_NODE_TYPE = new TestOutputNodeType();

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

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.INTEGER);
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

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.INTEGER);
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

    @Test
    public void graphWithPathIsValid() {
        var graph = new Graph();
        var input = graph.addNode(INPUT_NODE_TYPE);
        var output = graph.addNode(OUTPUT_NODE_TYPE);
        var conn = NodeConnection.createConnection(input, "out", output, "in", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
        Assertions.assertTrue(graph.validate());
    }

    @Test
    public void graphWithoutPathIsNotValid() {
        var graph = new Graph();
        var input = graph.addNode(INPUT_NODE_TYPE);
        var output = graph.addNode(OUTPUT_NODE_TYPE);
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithoutInputIsNotValid() {
        var graph = new Graph();
        var input = graph.addNode(TEST_NODE_TYPE);
        var output = graph.addNode(OUTPUT_NODE_TYPE);
        var conn = NodeConnection.createConnection(input, "out", output, "in", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithoutOutputIsNotValid() {
        var graph = new Graph();
        var input = graph.addNode(INPUT_NODE_TYPE);
        var output = graph.addNode(TEST_NODE_TYPE);
        var conn = NodeConnection.createConnection(input, "out", output, "in", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithLongerPathIsValid() {
        var graph = new Graph();
        var input = graph.addNode(INPUT_NODE_TYPE);
        var output = graph.addNode(OUTPUT_NODE_TYPE);

        @Nullable Node lastNode = null;
        for (int i = 0; i < 10; i++) {
            var intermediary = graph.addNode(TEST_NODE_TYPE);
            var conn = NodeConnection.createConnection(
                lastNode == null ? input : lastNode, "out",
                intermediary, "in",
                DataType.INTEGER
            );
            Assertions.assertTrue(conn.isPresent());
            graph.addConnection(conn.get());

            lastNode = intermediary;
        }

        var conn = NodeConnection.createConnection(
            lastNode, "out",
            output, "in",
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());

        Assertions.assertTrue(graph.validate());
    }

    private static final class TestNodeType implements NodeType {
        @Override
        public NodeCategory category() {
            return NodeCategory.PROCESS;
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

    private static final class TestInputNodeType implements NodeType {
        @Override
        public NodeCategory category() {
            return NodeCategory.PUSHED_INPUT;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }
    }

    private static final class TestOutputNodeType implements NodeType {
        @Override
        public NodeCategory category() {
            return NodeCategory.OUTPUT;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }
    }
}
