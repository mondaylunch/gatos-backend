package club.mondaylunch.gatos.core.graph.type.test;

import java.util.Map;
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
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static class TestProcessNodeType extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER));
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    private static class TestEndNodeType extends NodeType.End {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER));
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
