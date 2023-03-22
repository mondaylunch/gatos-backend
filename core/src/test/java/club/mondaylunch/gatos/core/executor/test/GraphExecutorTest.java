package club.mondaylunch.gatos.core.executor.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.executor.GraphExecutor;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;

public class GraphExecutorTest {
    private static final NodeType ADD_NUMS = new AddNumNodeType();
    private static final NodeType ADD_NUMS_SLOWLY = new SlowlyAddNumNodeType();
    private static final NodeType ADD_NUMS_2INPUT = new AddNumTwoInputNodeType();
    private static final NodeType INPUT_NUM = new InputNumNodeType();

    @Test
    public void simpleGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var input = graph.addNode(INPUT_NUM);
        graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.NUMBER.create(10.)));
        var output = graph.addNode(new OutputIntNodeType(result));

        var adder = graph.addNode(ADD_NUMS);
        graph.modifyNode(adder.id(), node -> node.modifySetting("value_to_add", DataType.NUMBER.create(5.)));

        var conn = NodeConnection.create(
            input, "out",
            adder, "in"
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);

        conn = NodeConnection.create(
            adder, "out",
            output, "in"
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isLeft());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.left(), graph.getConnections());

        graphExecutor.execute(UUID.randomUUID()).get().join();
        Assertions.assertEquals(15, result.get());
    }

    @Test
    public void slowGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var input = graph.addNode(INPUT_NUM);
        graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.NUMBER.create(10.)));
        var output = graph.addNode(new OutputIntNodeType(result));

        var adder = graph.addNode(ADD_NUMS_SLOWLY);
        graph.modifyNode(adder.id(), node -> node.modifySetting("value_to_add", DataType.NUMBER.create(5.)));

        var conn = NodeConnection.create(
            input, "out",
            adder, "in"
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);

        conn = NodeConnection.create(
            adder, "out",
            output, "in"
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isLeft());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.left(), graph.getConnections());

        graphExecutor.execute(UUID.randomUUID()).get().join();
        Assertions.assertEquals(15, result.get());
    }

    @Test
    public void complexGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var output = graph.addNode(new OutputIntNodeType(result));

        var inputs = IntStream.of(2, 4, 8, 16, 2)
            .mapToObj(i -> {
                var input = graph.addNode(INPUT_NUM);
                return graph.modifyNode(input.id(),
                    node -> node.modifySetting("value", DataType.NUMBER.create((double) i)));
            })
            .toList();

        var adder1 = graph.addNode(ADD_NUMS_2INPUT);
        var adder2 = graph.addNode(ADD_NUMS_2INPUT);
        var adder3 = graph.addNode(ADD_NUMS_2INPUT);
        var adder4 = graph.addNode(ADD_NUMS_2INPUT);

        connectInt(graph, inputs.get(1), "out", adder1, "in1");
        connectInt(graph, inputs.get(2), "out", adder1, "in2");

        connectInt(graph, inputs.get(3), "out", adder2, "in1");
        connectInt(graph, inputs.get(4), "out", adder2, "in2");

        connectInt(graph, adder1, "out", adder3, "in1");
        connectInt(graph, adder2, "out", adder3, "in2");

        connectInt(graph, adder3, "out", adder4, "in1");
        connectInt(graph, inputs.get(0), "out", adder4, "in2");

        connectInt(graph, adder4, "out", output, "in");

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isLeft());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.left(), graph.getConnections());

        graphExecutor.execute(UUID.randomUUID()).get().join();
        Assertions.assertEquals(32, result.get());
    }

    @Test
    public void complexGraphWithMultiConnectComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result1 = new AtomicInteger();
        var output1 = graph.addNode(new OutputIntNodeType(result1));
        AtomicInteger result2 = new AtomicInteger();
        var output2 = graph.addNode(new OutputIntNodeType(result2));

        var inputs = IntStream.of(2, 4, 8, 16, 2)
            .mapToObj(i -> {
                var input = graph.addNode(INPUT_NUM);
                return graph.modifyNode(input.id(),
                    node -> node.modifySetting("value", DataType.NUMBER.create((double) i)));
            })
            .toList();

        var adder1 = graph.addNode(ADD_NUMS_2INPUT);
        var adder2 = graph.addNode(ADD_NUMS_2INPUT);
        var adder3 = graph.addNode(ADD_NUMS_2INPUT);
        var adder4 = graph.addNode(ADD_NUMS_2INPUT);
        var adder5 = graph.addNode(ADD_NUMS_2INPUT);

        connectInt(graph, inputs.get(1), "out", adder1, "in1");
        connectInt(graph, inputs.get(2), "out", adder1, "in2");

        connectInt(graph, inputs.get(3), "out", adder2, "in1");
        connectInt(graph, inputs.get(4), "out", adder2, "in2");

        connectInt(graph, adder1, "out", adder3, "in1");
        connectInt(graph, adder2, "out", adder3, "in2");

        connectInt(graph, adder3, "out", adder4, "in1");
        connectInt(graph, inputs.get(0), "out", adder4, "in2");

        connectInt(graph, adder4, "out", output1, "in");

        connectInt(graph, adder4, "out", adder5, "in1");
        connectInt(graph, inputs.get(3), "out", adder5, "in2");

        connectInt(graph, adder5, "out", output2, "in");

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isLeft());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.left(), graph.getConnections());

        graphExecutor.execute(UUID.randomUUID()).get().join();
        Assertions.assertEquals(32, result1.get());
        Assertions.assertEquals(48, result2.get());
    }

    @Test
    public void complexGraphWithMultiConnectAndTypeConversionComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result1 = new AtomicInteger();
        var output1 = graph.addNode(new OutputIntNodeType(result1));
        AtomicReference<String> result2 = new AtomicReference<>("");
        var output2 = graph.addNode(new OutputStringNodeType(result2));

        var inputs = IntStream.of(2, 4, 8, 16, 2)
            .mapToObj(i -> {
                var input = graph.addNode(INPUT_NUM);
                return graph.modifyNode(input.id(),
                    node -> node.modifySetting("value", DataType.NUMBER.create((double) i)));
            })
            .toList();

        var adder1 = graph.addNode(ADD_NUMS_2INPUT);
        var adder2 = graph.addNode(ADD_NUMS_2INPUT);
        var adder3 = graph.addNode(ADD_NUMS_2INPUT);
        var adder4 = graph.addNode(ADD_NUMS_2INPUT);
        var adder5 = graph.addNode(ADD_NUMS_2INPUT);

        connectInt(graph, inputs.get(1), "out", adder1, "in1");
        connectInt(graph, inputs.get(2), "out", adder1, "in2");

        connectInt(graph, inputs.get(3), "out", adder2, "in1");
        connectInt(graph, inputs.get(4), "out", adder2, "in2");

        connectInt(graph, adder1, "out", adder3, "in1");
        connectInt(graph, adder2, "out", adder3, "in2");

        connectInt(graph, adder3, "out", adder4, "in1");
        connectInt(graph, inputs.get(0), "out", adder4, "in2");

        connectInt(graph, adder4, "out", output1, "in");

        connectInt(graph, adder4, "out", adder5, "in1");
        connectInt(graph, inputs.get(3), "out", adder5, "in2");

        connectString(graph, adder5, "out", output2, "in");

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isLeft());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.left(), graph.getConnections());

        graphExecutor.execute(UUID.randomUUID()).get().join();
        Assertions.assertEquals(32, result1.get());
        Assertions.assertEquals("48.0", result2.get());
    }

    private static void connectInt(Graph graph, Node a, String connectorA, Node b, String connectorB) {
        var conn = NodeConnection.create(
            a, connectorA,
            b, connectorB
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);
    }

    private static void connectString(Graph graph, Node a, String connectorA, Node b, String connectorB) {
        var conn = NodeConnection.create(
            a, connectorA,
            b, connectorB
        );
        Assertions.assertTrue(true);
        graph.addConnection(conn);
    }

    private static final class AddNumNodeType extends NodeType.Process {
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
                "value_to_add", DataType.NUMBER.create((double) 0));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of(
                "out", CompletableFuture.completedFuture(DataType.NUMBER.create(
                    DataBox.get(settings, "value_to_add", DataType.NUMBER).orElseThrow()
                        + DataBox.get(inputs, "in", DataType.NUMBER).orElseThrow())));
        }
    }

    private static final class AddNumTwoInputNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in1", DataType.NUMBER),
                new NodeConnector.Input<>(nodeId, "in2", DataType.NUMBER));
        }

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
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of(
                "out", CompletableFuture.completedFuture(DataType.NUMBER.create(
                    DataBox.get(inputs, "in1", DataType.NUMBER).orElseThrow()
                        + DataBox.get(inputs, "in2", DataType.NUMBER).orElseThrow())));
        }
    }

    private static final class SlowlyAddNumNodeType extends NodeType.Process {
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
                "value_to_add", DataType.NUMBER.create((double) 0));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs,
                                                                  Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Map.of(
                "out", CompletableFuture.supplyAsync(this.addIntsSlowly(
                    DataBox.get(settings, "value_to_add", DataType.NUMBER).orElseThrow(),
                    DataBox.get(inputs, "in", DataType.NUMBER).orElseThrow())));
        }

        private Supplier<DataBox<?>> addIntsSlowly(double a, double b) {
            return () -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return DataType.NUMBER.create(a + b);
            };
        }
    }

    private static final class InputNumNodeType extends NodeType.Start<Object> {
        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.NUMBER));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "value", DataType.NUMBER.create(0.));
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, @Nullable Object input, Map<String, DataBox<?>> settings) {
            return Map.of(
                "out", CompletableFuture.completedFuture(
                    DataType.NUMBER.create(DataBox.get(settings, "value", DataType.NUMBER).orElseThrow())));
        }

        @Override
        public void setupFlow(Flow flow, Consumer<@Nullable Object> function, Node node) {
        }

        @Override
        public void teardownFlow(Flow flow, Node node) {

        }
    }

    private static final class OutputIntNodeType extends NodeType.End {
        public final AtomicInteger result;

        // obviously outside of tests we would not be instantiating node types multiple
        // times...
        private OutputIntNodeType(AtomicInteger result) {
            this.result = result;
        }

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
        public CompletableFuture<Void> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            this.result.set(DataBox.get(inputs, "in", DataType.NUMBER).orElseThrow().intValue());
            return CompletableFuture.runAsync(() -> {
            });
        }
    }

    private static final class OutputStringNodeType extends NodeType.End {
        public final AtomicReference<String> result;

        // obviously outside of tests we would not be instantiating node types multiple
        // times...
        private OutputStringNodeType(AtomicReference<String> result) {
            this.result = result;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.STRING));
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public CompletableFuture<Void> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            this.result.set(DataBox.get(inputs, "in", DataType.STRING).orElseThrow());
            return CompletableFuture.runAsync(() -> {
            });
        }
    }
}
