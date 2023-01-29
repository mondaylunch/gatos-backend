package gay.oss.gatos.core.graph.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.NodeCategory;
import gay.oss.gatos.core.graph.NodeType;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;

public class NodeConnectionTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();

    @Test
    public void canCreateConnection() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "out", node2, "in", DataType.INTEGER);
        Assertions.assertTrue(conn.isPresent());
    }

    @Test
    public void connectionWithNonexistentConnectorIsEmpty() {
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = Node.create(TEST_NODE_TYPE);

        var conn = NodeConnection.createConnection(node1, "invalid", node2, "in", DataType.INTEGER);
        Assertions.assertTrue(conn.isEmpty());

        conn = NodeConnection.createConnection(node1, "out", node2, "invalid", DataType.INTEGER);
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
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER),
                new NodeConnector.Output<>(nodeId, "out_2", DataType.BOOLEAN)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }
}
