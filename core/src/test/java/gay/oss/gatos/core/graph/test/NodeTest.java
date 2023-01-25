package gay.oss.gatos.core.graph.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.NodeCategory;
import gay.oss.gatos.core.graph.NodeType;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;

public class NodeTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();

    @Test
    public void inputsShouldExist() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.getInputWithName("in").isPresent());
    }

    @Test
    public void outputsShouldExist() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.getOutputWithName("out").isPresent());
    }

    @Test
    public void settingsShouldExist() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertEquals(0, node.getSetting("setting_1", Integer.class).value());
    }

    @Test
    public void canMakeNewNodeWithChangedSettings() {
        var node = Node.create(TEST_NODE_TYPE);
        var newNode = node.modifySetting("setting_1", 100);
        Assertions.assertEquals(node.id(), newNode.id());
        Assertions.assertEquals(100, newNode.getSetting("setting_1", Integer.class).value());
    }

    @Test
    public void connectorsChangeBasedOnSettings() {
        var node = Node.create(TEST_NODE_TYPE);
        var newNode = node.modifySetting("setting_2", true);
        Assertions.assertFalse(node.getOutputWithName("out_2").isPresent());
        Assertions.assertTrue(newNode.getOutputWithName("out_2").isPresent());
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
            var out = new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER);
            return (Boolean) state.get("setting_2").value()
                    ? Set.of(
                    out,
                    new NodeConnector.Output<>(nodeId, "out_2", DataType.INTEGER)
            )
                    : Set.of(
                    out
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                    "setting_1", DataType.INTEGER.create(0),
                    "setting_2", DataType.BOOLEAN.create(false)
            );
        }
    }
}
