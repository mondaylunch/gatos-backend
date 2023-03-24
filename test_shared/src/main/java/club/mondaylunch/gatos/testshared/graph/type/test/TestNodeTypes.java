package club.mondaylunch.gatos.testshared.graph.type.test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class TestNodeTypes {

    public static final NodeType.Process NO_INPUTS = NodeType.REGISTRY.register("test_no_inputs", new TestNoInputNodeType());
    public static final NodeType.Process PROCESS = NodeType.REGISTRY.register("test_process", new TestProcessNodeType());
    public static final NodeType.Process MULTIPLE_CONNECTIONS = NodeType.REGISTRY.register("test_multiple_connections", new TestMultipleConnections());
    public static final NodeType.End END = NodeType.REGISTRY.register("test_end", new TestEndNodeType(DataType.NUMBER));
    public static final NodeType.End END_STRING = NodeType.REGISTRY.register("test_end_string", new TestEndNodeType(DataType.STRING));
    public static final NodeType TEST_VARYING_OUTPUT_NODE_TYPE = NodeType.REGISTRY.register("test_varying_inputs", new TestVaryingOutputNodeType());
    @SuppressWarnings("unused")
    public static final NodeType TEST_TYPE_SPECIALISED_NODE_TYPE = NodeType.REGISTRY.register("test_specialised", new TestTypeSpecialisedNodeType());

    private static class TestNoInputNodeType extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "setting", DataType.NUMBER.create(0.0)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(new NodeConnector.Output<>(nodeId, "start_output", DataType.NUMBER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of();
        }
    }

    private static class TestProcessNodeType extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "setting", DataType.NUMBER.listOf().create(List.of(20.0, 3.0))
            );
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(new NodeConnector.Input<>(nodeId, "process_input", DataType.NUMBER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(new NodeConnector.Output<>(nodeId, "process_output", DataType.NUMBER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }

    private static class TestMultipleConnections extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "input_1", DataType.NUMBER),
                new NodeConnector.Input<>(nodeId, "input_2", DataType.NUMBER),
                new NodeConnector.Input<>(nodeId, "input_3", DataType.NUMBER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "output_1", DataType.NUMBER),
                new NodeConnector.Output<>(nodeId, "output_2", DataType.NUMBER),
                new NodeConnector.Output<>(nodeId, "output_3", DataType.NUMBER)
            );
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }

    private static class TestEndNodeType extends NodeType.End {

        private final DataType<?> type;

        private TestEndNodeType(DataType<?> type) {
            this.type = type;
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "setting", DataType.NUMBER.optionalOf().create(Optional.of(1.5))
            );
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(new NodeConnector.Input<>(nodeId, "end_input", this.type));
        }

        @Override
        public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class TestTypeSpecialisedNodeType extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var typeA = inputTypes.getOrDefault("input_a", DataType.ANY);
            var typeB = inputTypes.getOrDefault("input_b", DataType.ANY);
            DataType<?> returnType = DataType.ANY;
            if (!(typeA.equals(DataType.ANY))) {
                returnType = typeA;
            } else if (!(typeB.equals(DataType.ANY))) {
                returnType = typeB;
            }

            return Set.of(
                new NodeConnector.Input<>(nodeId, "input_a", returnType),
                new NodeConnector.Input<>(nodeId, "input_b", returnType)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var typeA = inputTypes.getOrDefault("input_a", DataType.ANY);
            var typeB = inputTypes.getOrDefault("input_b", DataType.ANY);
            DataType<?> returnType = DataType.ANY;
            if (!(typeA.equals(DataType.ANY))) {
                returnType = typeA;
            } else if (!(typeB.equals(DataType.ANY))) {
                returnType = typeB;
            }

            return Set.of(new NodeConnector.Output<>(nodeId, "process_output", returnType));
        }

        @SuppressWarnings("unused")
        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }

    public static final class TestVaryingOutputNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var inType = inputTypes.getOrDefault("in", DataType.ANY);
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", inType));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            var outType = inputTypes.getOrDefault("in", DataType.ANY);
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", outType));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings,
                                                                  Map<String, DataType<?>> inputTypes) {
            return Map.of();
        }
    }
}
