package gay.oss.gatos.core.graph.test;

import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.setting.BooleanNodeSetting;
import gay.oss.gatos.core.graph.setting.IntegerNodeSetting;
import gay.oss.gatos.core.graph.setting.NodeSetting;
import gay.oss.gatos.core.graph.NodeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        Assertions.assertTrue(node.getOutputWithName("output").isPresent());
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
        public boolean hasInputs() {
            return true;
        }

        @Override
        public boolean hasOutputs() {
            return true;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, NodeSetting<?>> state) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in")
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, NodeSetting<?>> state) {
            var out = new NodeConnector.Output<>(nodeId, "out");
            return (Boolean) state.get("setting_2").value()
                    ? Set.of(
                    out,
                    new NodeConnector.Output<>(nodeId, "out_2")
            )
                    : Set.of(
                    out
            );
        }

        @Override
        public Map<String, NodeSetting<?>> settings() {
            return Map.of(
                    "setting_1", new IntegerNodeSetting(0),
                    "setting_2", new BooleanNodeSetting(false)
            );
        }
    }
}
