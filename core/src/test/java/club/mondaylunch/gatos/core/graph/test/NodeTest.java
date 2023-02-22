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
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class NodeTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();

    @Test
    public void canGetInputs() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.inputs().containsKey("in"));
    }

    @Test
    public void canGetInputByName() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.getInputWithName("in").isPresent());
    }

    @Test
    public void canGetOutputs() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.getOutputs().containsKey("out"));
    }

    @Test
    public void canGetOutputByName() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertTrue(node.getOutputWithName("out").isPresent());
    }

    @Test
    public void canGetSettingByName() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertEquals(0, node.getSetting("setting_1", DataType.NUMBER).value());
    }

    @Test
    public void gettingWrongSettingThrows() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            node.getSetting("setting_3", DataType.NUMBER);
        });
    }

    @Test
    public void gettingWrongTypeSettingThrows() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            node.getSetting("setting_2", DataType.NUMBER);
        });
    }

    @Test
    public void canMakeNewNodeWithChangedSettings() {
        var node = Node.create(TEST_NODE_TYPE);
        var newNode = node.modifySetting("setting_1", DataType.NUMBER.create(100.));
        Assertions.assertEquals(node.id(), newNode.id());
        Assertions.assertEquals(100, newNode.getSetting("setting_1", DataType.NUMBER).value());
    }

    @Test
    public void changingNonexistentSettingThrows() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            node.modifySetting("setting_3", DataType.NUMBER.create(100.));
        });
    }

    @Test
    public void changingWrongTypeSettingThrows() {
        var node = Node.create(TEST_NODE_TYPE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            node.modifySetting("setting_2", DataType.NUMBER.create(100.));
        });
    }

    @Test
    public void connectorsChangeBasedOnSettings() {
        var node = Node.create(TEST_NODE_TYPE);
        var newNode = node.modifySetting("setting_2", DataType.BOOLEAN.create(true));
        Assertions.assertFalse(node.getOutputWithName("out_2").isPresent());
        Assertions.assertTrue(newNode.getOutputWithName("out_2").isPresent());
    }

    private static final class TestNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in", DataType.NUMBER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var out = new NodeConnector.Output<>(nodeId, "out", DataType.NUMBER);
            return (Boolean) settings.get("setting_2").value()
                    ? Set.of(
                            out,
                            new NodeConnector.Output<>(nodeId, "out_2", DataType.NUMBER))
                    : Set.of(
                            out);
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                    "setting_1", DataType.NUMBER.create(0.),
                    "setting_2", DataType.BOOLEAN.create(false));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }
}
