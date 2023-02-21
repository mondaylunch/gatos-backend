package club.mondaylunch.gatos.core.graph.type.test;

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

    public static final NodeType.Start START = NodeType.REGISTRY.register("test_start", new TestStartNodeType());
    public static final NodeType.Process PROCESS = NodeType.REGISTRY.register("test_process", new TestProcessNodeType());
    public static final NodeType.End END = NodeType.REGISTRY.register("test_end", new TestEndNodeType());

    private static class TestStartNodeType extends NodeType.Start {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "setting", DataType.NUMBER.create(0.0)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Output<>(nodeId, "start_output", DataType.NUMBER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
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
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Input<>(nodeId, "process_input", DataType.NUMBER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Output<>(nodeId, "process_output", DataType.NUMBER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static class TestEndNodeType extends NodeType.End {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "setting", DataType.NUMBER.optionalOf().create(Optional.of(1.5))
            );
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Input<>(nodeId, "end_input", DataType.NUMBER));
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
