package club.mondaylunch.gatos.core.graph.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;

public class GraphTest {
    private static final NodeType TEST_NODE_TYPE = new TestNodeType();
    private static final NodeType START_NODE_TYPE = new TestStartNodeType();
    private static final NodeType END_NODE_TYPE = new TestEndNodeType();
    private static final NodeType TEST_VARYING_OUTPUT_NODE_TYPE = new TestVaryingOutputNodeType();
    private static final NodeType TEST_STRING_OPT_START_NODE_TYPE = new TestStringOptStartNodeType();
    private static final NodeType START_TWO_OUTPUTS_NODE_TYPE = new TestStartTwoOutputsNodeType();
    private static final NodeType END_TWO_INPUTS_NODE_TYPE = new TestEndTwoInputsNodeType();

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
        var modifiedNode = graph.modifyNode(node.id(), n -> n.modifySetting("setting_1", DataType.NUMBER.create(100.)));
        Assertions.assertFalse(graph.containsNode(node));
        Assertions.assertTrue(graph.containsNode(node.id()));
        Assertions.assertTrue(graph.containsNode(modifiedNode));
        Assertions.assertEquals(100, modifiedNode.getSetting("setting_1", DataType.NUMBER).value());
    }

    @Test
    public void modifyingNonexistentNodeThrows() {
        var graph = new Graph();
        graph.addNode(TEST_NODE_TYPE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            graph.modifyNode(UUID.randomUUID(), n -> n.modifySetting("setting_1", DataType.NUMBER.create(100.)));
        });
    }

    @Test
    public void modifyingNodeWithNullThrows() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        Assertions.assertThrows(NullPointerException.class, () -> {
            graph.modifyNode(node.id(), n -> null);
        });
    }

    @Test
    public void canAddConnection() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.create(node1, "out", node2, "in");

        graph.addConnection(conn);

        Assertions.assertTrue(graph.getConnectionsForNode(node1.id()).contains(conn));
        Assertions.assertTrue(graph.getConnectionsForNode(node2.id()).contains(conn));
    }

    @Test
    public void addingConnectionWithNonexistentNodeThrows() {
        var graph = new Graph();
        var node1 = Node.create(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn1 = NodeConnection.create(node1, "out", node2, "in");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            graph.addConnection(conn1);
        });

        var conn2 = NodeConnection.create(node2, "out", node1, "in");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            graph.addConnection(conn2);
        });
    }

    @Test
    public void multipleConnectionsToOneConnectorThrows() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);
        var node3 = graph.addNode(TEST_NODE_TYPE);

        var conn1 = NodeConnection.create(node1, "out", node3, "in");

        graph.addConnection(conn1);

        var conn2 = NodeConnection.create(node2, "out", node3, "in");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            graph.addConnection(conn2);
        });
    }

    @Test
    public void canRemoveConnection() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.create(node1, "out", node2, "in");

        graph.addConnection(conn);
        graph.removeConnection(conn);

        Assertions.assertFalse(graph.getConnectionsForNode(node1.id()).contains(conn));
        Assertions.assertFalse(graph.getConnectionsForNode(node2.id()).contains(conn));
    }

    @Test
    public void removingSourceNodeRemovesConnections() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.create(node1, "out", node2, "in");

        graph.addConnection(conn);

        graph.removeNode(node1.id());

        Assertions.assertFalse(graph.getConnectionsForNode(node1.id()).contains(conn));
        Assertions.assertFalse(graph.getConnectionsForNode(node2.id()).contains(conn));
    }

    @Test
    public void removingDestinationNodeRemovesConnections() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_NODE_TYPE);
        var node2 = graph.addNode(TEST_NODE_TYPE);

        var conn = NodeConnection.create(node1, "out", node2, "in");

        graph.addConnection(conn);

        graph.removeNode(node2.id());

        Assertions.assertFalse(graph.getConnectionsForNode(node1.id()).contains(conn));
        Assertions.assertFalse(graph.getConnectionsForNode(node2.id()).contains(conn));
    }

    @Test
    public void makingConnectionWithSpecificTypeChangesOutputType() {
        var graph = new Graph();
        var node1 = graph.addNode(TEST_STRING_OPT_START_NODE_TYPE);
        var node2 = graph.addNode(TEST_VARYING_OUTPUT_NODE_TYPE);

        Assertions.assertEquals(DataType.ANY, node2.getOutputs().get("out").type());
        var conn = NodeConnection.create(node1, "out", node2, "in");
        graph.addConnection(conn);
        Assertions.assertEquals(DataType.STRING, graph.getNode(node2.id()).orElseThrow().getOutputs().get("out").type());
        graph.removeConnection(conn);
        Assertions.assertEquals(DataType.ANY, graph.getNode(node2.id()).orElseThrow().getOutputs().get("out").type());
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
    public void modifyingMetadataWithNullThrows() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        Assertions.assertThrows(NullPointerException.class, () -> {
            graph.modifyMetadata(node.id(), n -> null);
        });
    }

    @Test
    public void removingNodeRemovesMetadata() {
        var graph = new Graph();
        var node = graph.addNode(TEST_NODE_TYPE);
        var res = graph.modifyMetadata(node.id(), meta -> meta.withX(10f).withY(20f));
        graph.removeNode(node.id());
        Assertions.assertNotEquals(res, graph.getOrCreateMetadataForNode(node.id()));
    }

    @Test
    public void graphWithPathIsValid() {
        var graph = new Graph();
        var input = graph.addNode(START_NODE_TYPE);
        var output = graph.addNode(END_NODE_TYPE);
        var conn = NodeConnection.create(input, "out", output, "in");
        graph.addConnection(conn);
        Assertions.assertTrue(graph.validate());
    }

    @Test
    public void graphWithoutPathIsNotValid() {
        var graph = new Graph();
        graph.addNode(START_NODE_TYPE);
        graph.addNode(END_NODE_TYPE);
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithoutInputIsNotValid() {
        var graph = new Graph();
        var input = graph.addNode(TEST_NODE_TYPE);
        var output = graph.addNode(END_NODE_TYPE);
        var conn = NodeConnection.create(input, "out", output, "in");
        graph.addConnection(conn);
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithoutOutputIsNotValid() {
        var graph = new Graph();
        var input = graph.addNode(START_NODE_TYPE);
        var output = graph.addNode(TEST_NODE_TYPE);
        var conn = NodeConnection.create(input, "out", output, "in");
        graph.addConnection(conn);
        Assertions.assertFalse(graph.validate());
    }

    @Test
    public void graphWithLongerPathIsValid() {
        var graph = new Graph();
        var input = graph.addNode(START_NODE_TYPE);
        var output = graph.addNode(END_NODE_TYPE);

        @Nullable
        Node lastNode = null;
        for (int i = 0; i < 10; i++) {
            var intermediary = graph.addNode(TEST_NODE_TYPE);
            var conn = NodeConnection.create(
                    lastNode == null ? input : lastNode, "out",
                    intermediary, "in"
            );
            graph.addConnection(conn);

            lastNode = intermediary;
        }

        var conn = NodeConnection.create(
                lastNode, "out",
                output, "in"
            );
        graph.addConnection(conn);

        Assertions.assertTrue(graph.validate());
    }

    @Test
    public void graphWithLongerPathAndExtraNodesIsValid() {
        var graph = new Graph();
        var input = graph.addNode(START_NODE_TYPE);
        var output = graph.addNode(END_NODE_TYPE);

        @Nullable
        Node lastNode = null;
        for (int i = 0; i < 10; i++) {
            var intermediary = graph.addNode(TEST_NODE_TYPE);
            var conn = NodeConnection.create(
                    lastNode == null ? input : lastNode, "out",
                    intermediary, "in"
            );
            graph.addConnection(conn);

            lastNode = intermediary;
        }

        var conn = NodeConnection.create(
                lastNode, "out",
                output, "in"
        );
        graph.addConnection(conn);

        for (int i = 0; i < 10; i++) {
            graph.addNode(TEST_NODE_TYPE);
        }

        Assertions.assertTrue(graph.validate());
    }

    @Test
    public void graphHasCorrectPath() {
        var graph = new Graph();
        var input = graph.addNode(START_NODE_TYPE);
        var output = graph.addNode(END_NODE_TYPE);
        List<Node> list = new ArrayList<>();
        list.add(input);

        @Nullable
        Node lastNode = null;
        for (int i = 0; i < 10; i++) {
            var intermediary = graph.addNode(TEST_NODE_TYPE);
            var conn = NodeConnection.create(
                    lastNode == null ? input : lastNode, "out",
                    intermediary, "in"
            );
            graph.addConnection(conn);

            lastNode = intermediary;
            list.add(intermediary);
        }

        var conn = NodeConnection.create(
                lastNode, "out",
                output, "in"
        );
        graph.addConnection(conn);

        list.add(output);

        for (int i = 0; i < 10; i++) {
            graph.addNode(TEST_NODE_TYPE);
        }

        var sorted = graph.getExecutionOrder();
        Assertions.assertTrue(sorted.isPresent());
        Assertions.assertEquals(list, sorted.get());
    }

    @Test
    public void graphWithMultiplePathsBetweenSameNodesIsValid() {
        var graph = new Graph();
        var input = graph.addNode(START_TWO_OUTPUTS_NODE_TYPE);
        var output = graph.addNode(END_TWO_INPUTS_NODE_TYPE);
        var conn = NodeConnection.create(input, "out1", output, "in1");
        graph.addConnection(conn);
        conn = NodeConnection.create(input, "out2", output, "in2");
        graph.addConnection(conn);
        Assertions.assertTrue(graph.validate());
    }

    private static final class TestNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in", DataType.NUMBER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Output<>(nodeId, "out", DataType.NUMBER));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                    "setting_1", DataType.NUMBER.create(0.));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }

    private static final class TestStartNodeType extends NodeType.Start<Object> {
        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Output<>(nodeId, "out", DataType.NUMBER));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public void setupFlow(Flow flow, Consumer<@Nullable Object> function, Node node) {

        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(@Nullable Object o, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static final class TestEndNodeType extends NodeType.End {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in", DataType.NUMBER));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.runAsync(() -> {
            });
        }
    }

    private static final class TestVaryingOutputNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", OptionalDataType.GENERIC_OPTIONAL));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var inputOptType = inputTypes.getOrDefault("in", OptionalDataType.GENERIC_OPTIONAL);
            DataType<?> outType;
            if (inputOptType == OptionalDataType.GENERIC_OPTIONAL) {
                outType = DataType.ANY;
            } else {
                outType = ((OptionalDataType<?>) inputOptType).contains();
            }
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", outType));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings,
                                                                  Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }

    private static final class TestStringOptStartNodeType extends NodeType.Start<Object> {
        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(new NodeConnector.Output<>(nodeId, "out", DataType.STRING.optionalOf()));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public void setupFlow(Flow flow, Consumer<@Nullable Object> function, Node node) {
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(@Nullable Object o, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static final class TestStartTwoOutputsNodeType extends NodeType.Start<Object> {
        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Output<>(nodeId, "out1", DataType.NUMBER),
                    new NodeConnector.Output<>(nodeId, "out2", DataType.NUMBER)
            );
        }

        @Override
        public void setupFlow(Flow flow, Consumer<@Nullable Object> function, Node node) {
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(@Nullable Object o, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static final class TestEndTwoInputsNodeType extends NodeType.End {
        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                    new NodeConnector.Input<>(nodeId, "in1", DataType.NUMBER),
                    new NodeConnector.Input<>(nodeId, "in2", DataType.NUMBER)
            );
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.runAsync(() -> {
            });
        }
    }
}
