package club.mondaylunch.gatos.core.graph.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class NodeConnectionTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();
    private static final NodeType TEST_NODE_TYPE_2 = new TestNodeType2();

    @Test
    public void canCreateConnection() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.NUMBER);
        Assertions.assertTrue(conn.isPresent());
    }

    @Test
    public void connectionWithNonexistentConnectorIsEmpty() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "invalid", node2, "in", DataType.NUMBER);
        Assertions.assertTrue(conn.isEmpty());

        conn = NodeConnection.createConnection(node1, "out", node2, "invalid", DataType.NUMBER);
        Assertions.assertTrue(conn.isEmpty());
    }

    @Test
    public void connectionWithWrongTypeIsEmpty() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.BOOLEAN);
        Assertions.assertTrue(conn.isEmpty());
    }

    @Test
    public void connectionWithNonMatchingTypesIsEmpty() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "out_2", node2, "in", DataType.BOOLEAN);
        Assertions.assertTrue(conn.isEmpty());
    }

    @Test
    public void connectionWithConvertableTypesIsNotEmpty() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE_2);

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.STRING);
        Assertions.assertFalse(conn.isEmpty());
    }

    private static final class TestNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.NUMBER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.NUMBER),
                new NodeConnector.Output<>(nodeId, "out_2", DataType.BOOLEAN)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static final class TestNodeType2 extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.STRING));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }
}
