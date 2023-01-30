package gay.oss.gatos.core.executor.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.executor.GraphExecutor;
import gay.oss.gatos.core.graph.Graph;
import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;
import gay.oss.gatos.core.graph.type.NodeType;

public class GraphExecutorTest {
    private static final NodeType ADD_INTS = new AddIntNodeType();
    private static final NodeType ADD_INTS_SLOWLY = new SlowlyAddIntNodeType();
    private static final NodeType ADD_INTS_2INPUT = new AddIntTwoInputNodeType();
    private static final NodeType INPUT_INT = new InputIntNodeType();

    @Test
    public void simpleGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var input = graph.addNode(INPUT_INT);
        graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.INTEGER.create(10)));
        var output = graph.addNode(new OutputIntNodeType(result));

        var adder = graph.addNode(ADD_INTS);
        graph.modifyNode(adder.id(), node -> node.modifySetting("value_to_add", DataType.INTEGER.create(5)));

        var conn = NodeConnection.createConnection(
            input, "out",
            adder, "in",
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());

        conn = NodeConnection.createConnection(
            adder, "out",
            output, "in",
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isPresent());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.get(), graph.getConnections());

        CompletableFuture.runAsync(graphExecutor.execute()).join();
        Assertions.assertEquals(15, result.get());
    }

    @Test
    public void slowGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var input = graph.addNode(INPUT_INT);
        graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.INTEGER.create(10)));
        var output = graph.addNode(new OutputIntNodeType(result));

        var adder = graph.addNode(ADD_INTS_SLOWLY);
        graph.modifyNode(adder.id(), node -> node.modifySetting("value_to_add", DataType.INTEGER.create(5)));

        var conn = NodeConnection.createConnection(
            input, "out",
            adder, "in",
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());

        conn = NodeConnection.createConnection(
            adder, "out",
            output, "in",
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());

        var executionOrderedNodes = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrderedNodes.isPresent());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.get(), graph.getConnections());

        CompletableFuture.runAsync(graphExecutor.execute()).join();
        Assertions.assertEquals(15, result.get());
    }

    @Test
    public void complexGraphComputesCorrectly() {
        var graph = new Graph();
        AtomicInteger result = new AtomicInteger();
        var output = graph.addNode(new OutputIntNodeType(result));

        var inputs = IntStream.of(2, 4, 8, 16, 2)
            .mapToObj(i -> {
                var input = graph.addNode(INPUT_INT);
                return graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.INTEGER.create(i)));
            })
            .toList();

        var adder1 = graph.addNode(ADD_INTS_2INPUT);
        var adder2 = graph.addNode(ADD_INTS_2INPUT);
        var adder3 = graph.addNode(ADD_INTS_2INPUT);
        var adder4 = graph.addNode(ADD_INTS_2INPUT);

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
        Assertions.assertTrue(executionOrderedNodes.isPresent());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.get(), graph.getConnections());

        CompletableFuture.runAsync(graphExecutor.execute()).join();
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
                var input = graph.addNode(INPUT_INT);
                return graph.modifyNode(input.id(), node -> node.modifySetting("value", DataType.INTEGER.create(i)));
            })
            .toList();

        var adder1 = graph.addNode(ADD_INTS_2INPUT);
        var adder2 = graph.addNode(ADD_INTS_2INPUT);
        var adder3 = graph.addNode(ADD_INTS_2INPUT);
        var adder4 = graph.addNode(ADD_INTS_2INPUT);
        var adder5 = graph.addNode(ADD_INTS_2INPUT);

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
        Assertions.assertTrue(executionOrderedNodes.isPresent());
        var graphExecutor = new GraphExecutor(executionOrderedNodes.get(), graph.getConnections());

        CompletableFuture.runAsync(graphExecutor.execute()).join();
        Assertions.assertEquals(32, result1.get());
        Assertions.assertEquals(48, result2.get());
    }

    private static void connectInt(Graph graph, Node a, String connectorA, Node b, String connectorB) {
        var conn = NodeConnection.createConnection(
            a, connectorA,
            b, connectorB,
            DataType.INTEGER
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
    }

    private static final class AddIntNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "value_to_add", DataType.INTEGER.create(0)
            );
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of(
                "out", CompletableFuture.completedFuture(DataType.INTEGER.create(
                        DataBox.get(settings, "value_to_add", DataType.INTEGER).orElseThrow()
                        + DataBox.get(inputs, "in", DataType.INTEGER).orElseThrow()
                    ))
            );
        }
    }

    private static final class AddIntTwoInputNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in1", DataType.INTEGER),
                new NodeConnector.Input<>(nodeId, "in2", DataType.INTEGER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of(
                "out", CompletableFuture.completedFuture(DataType.INTEGER.create(
                    DataBox.get(inputs, "in1", DataType.INTEGER).orElseThrow()
                        + DataBox.get(inputs, "in2", DataType.INTEGER).orElseThrow()
                ))
            );
        }
    }

    private static final class SlowlyAddIntNodeType extends NodeType.Process {
        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER)
            );
        }

        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "value_to_add", DataType.INTEGER.create(0)
            );
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of(
                "out", CompletableFuture.supplyAsync(this.addIntsSlowly(
                    DataBox.get(settings, "value_to_add", DataType.INTEGER).orElseThrow(),
                    DataBox.get(inputs, "in", DataType.INTEGER).orElseThrow()
                )
            ));
        }

        private Supplier<DataBox<?>> addIntsSlowly(int a, int b) {
            return () -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return DataType.INTEGER.create(a + b);
            };
        }
    }

    private static final class InputIntNodeType extends NodeType.Start {
        @Override
        public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Output<>(nodeId, "out", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of(
                "value", DataType.INTEGER.create(0)
            );
        }

        @Override
        public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            return Map.of(
                "out", CompletableFuture.completedFuture(DataType.INTEGER.create(DataBox.get(settings, "value", DataType.INTEGER).orElseThrow()))
            );
        }
    }

    private static final class OutputIntNodeType extends NodeType.End {
        public final AtomicInteger result;

        // obviously outside of tests we would not be instantiating node types multiple times...
        private OutputIntNodeType(AtomicInteger result) {
            this.result = result;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.INTEGER)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            this.result.set(DataBox.get(inputs, "in", DataType.INTEGER).orElseThrow());
            return CompletableFuture.runAsync(() -> {});
        }
    }
}
