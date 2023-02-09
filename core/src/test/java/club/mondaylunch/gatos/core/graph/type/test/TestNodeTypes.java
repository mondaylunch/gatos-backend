package club.mondaylunch.gatos.core.graph.type.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class TestNodeTypes {

    public static final NodeType.Start START = new TestStartNodeType();
    public static final NodeType.Process PROCESS = new TestProcessNodeType();
    public static final NodeType.End END = new TestEndNodeType();

    public static class TestStartNodeType extends NodeType.Start {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public String name() {
            return "test_start";
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    public static class TestProcessNodeType extends NodeType.Process {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public String name() {
            return "test_process";
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of();
        }
    }

    public static class TestEndNodeType extends NodeType.End {

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public String name() {
            return "test_end";
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of();
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
