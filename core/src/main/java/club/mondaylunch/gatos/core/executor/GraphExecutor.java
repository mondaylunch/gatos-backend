package club.mondaylunch.gatos.core.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeCategory;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * Handles execution of a {@link club.mondaylunch.gatos.core.graph.Graph flow
 * graph}.
 */
public class GraphExecutor {
    private final @Unmodifiable Collection<Node> nonEndNodes;
    private final @Unmodifiable Collection<Node> endNodes;
    private final @Unmodifiable Collection<Node> startNodes;
    private final @Unmodifiable Collection<NodeConnection<?>> connections;
    private final @Unmodifiable Map<Node, Collection<NodeConnection<?>>> nodeDependencies;

    /**
     * Creates a new GraphExecutor.
     *
     * @param nodes       the nodes to execute, in execution order
     * @param connections a set of all connections between nodes
     */
    public GraphExecutor(List<Node> nodes, Set<NodeConnection<?>> connections) {
        this.connections = List.copyOf(connections);
        this.nodeDependencies = nodes.stream().collect(Collectors.toMap(
            Function.identity(),
            n -> n.inputs().values().stream().flatMap(
                    connector -> this.connections.stream().filter(connection -> connection.to().equals(connector)))
                .toList()));
        this.nonEndNodes = nodes.stream().filter(n -> n.type() instanceof NodeType.WithOutputs).toList();
        this.endNodes = nodes.stream().filter(n -> n.type().category() == NodeCategory.END).toList();
        this.startNodes = nodes.stream().filter(n -> n.type().category() == NodeCategory.START).toList();
    }

    /**
     * Creates a new {@code GraphExecutor}.
     *
     * @param graph The graph to execute.
     * @throws IllegalArgumentException If the graph is {@link Graph#validate() invalid}.
     */
    public GraphExecutor(Graph graph) {
        this(graph.getExecutionOrder().maybeL().orElseThrow(() -> new IllegalArgumentException("Graph is invalid")), graph.getConnections());
    }

    /**
     * Creates a function which, when run, executes this flow graph asynchronously, from a certain input node using a given input.
     *
     * @param <T>           the type of the input
     * @param flowId        the UUID of the flow that is being executed
     * @param triggerNodeId the UUID of the start node that should take in the input
     * @return an execution function
     */
    public <T> Function<@Nullable T, CompletableFuture<Void>> execute(UUID flowId, @Nullable UUID triggerNodeId) {
        final Node triggerNode;
        if (triggerNodeId != null) {
            triggerNode = this.startNodes.stream()
                .filter(n -> n.id().equals(triggerNodeId))
                .findAny()
                .orElse(null);
        } else {
            triggerNode = null;
        }

        return input -> {
            CompletableFuture<Map<NodeConnection<?>, DataBox<?>>> resultsFuture = CompletableFuture.completedFuture(new HashMap<>());
            for (var node : this.nonEndNodes) {
                CompletableFuture<Map<NodeConnection<?>, DataBox<?>>> resultFuture;
                if (node == triggerNode) {
                    @SuppressWarnings("unchecked")
                    var outputs = (((NodeType.Start<T>) node.type()).compute(flowId, input, node.settings()));
                    resultFuture = this.associateResultsWithConnections(node, allOf(outputs));
                } else {
                    var inputs = this.collectInputsForNode(node, resultsFuture);
                    resultFuture = this.getNodeResults(flowId, node, inputs);
                }
                resultsFuture = resultsFuture.thenCompose(results -> resultFuture.thenApply(result -> {
                    results.putAll(result);
                    return results;
                }));
            }
            final var finalResultsFuture = resultsFuture;
            var computedEndNodeFutures = this.endNodes.stream().map(node -> {
                var inputsFuture = this.collectInputsForNode(node, finalResultsFuture);
                return inputsFuture.thenCompose(inputs ->
                    ((NodeType.End) node.type()).compute(flowId, inputs, node.settings())
                );
            }).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(computedEndNodeFutures);
        };
    }

    /**
     * Convenience overload of {@link #execute(UUID, UUID)} which returns a Supplier instead, and triggers no start node.
     *
     * @param flowId the UUID of the flow that is being executed
     * @return a Supplier which, when run, executes this flow graph asynchronously
     */
    public Supplier<CompletableFuture<Void>> execute(UUID flowId) {
        var function = this.execute(flowId, null);
        return () -> function.apply(null);
    }

    /**
     * Returns a future of the dependencies of a node in
     * a map associated by their input connector name.
     *
     * @param node             the node
     * @param allResultsFuture a future of the map of node connection
     *                         results, for retrieving input values from
     * @return a future of the input values, associated by connector name
     */
    private CompletableFuture<Map<String, DataBox<?>>> collectInputsForNode(
        Node node,
        CompletableFuture<Map<NodeConnection<?>, DataBox<?>>> allResultsFuture
    ) {
        return allResultsFuture.thenApply(allResults -> {
            Map<String, DataBox<?>> inputs = new HashMap<>();
            for (var dep : this.nodeDependencies.get(node)) {
                var resForDep = allResults.get(dep);
                var depInputName = dep.to().name();
                inputs.put(depInputName, Conversions.convert(resForDep, dep.to().type()));
            }
            return inputs;
        });
    }

    /**
     * Creates future of a map of each of a node's
     * output connections to their output data.
     *
     * @param flowId       the UUID of the flow that is being executed
     * @param node         the node
     * @param inputsFuture a future of a map of inputs to the node
     * @return a map from each output connection to that connection's output
     */
    private CompletableFuture<Map<NodeConnection<?>, DataBox<?>>> getNodeResults(
        UUID flowId,
        Node node,
        CompletableFuture<Map<String, DataBox<?>>> inputsFuture
    ) {
        var resultsByConnectorNameFuture = inputsFuture.thenApply(inputs -> {
            Map<String, CompletableFuture<DataBox<?>>> resultsByConnectorName;
            if (node.type() instanceof NodeType.WithOutputs outputs) {
                resultsByConnectorName = outputs.compute(flowId, inputs, node.settings(), node.inputTypes());
            } else {
                resultsByConnectorName = Map.of();
            }
            return resultsByConnectorName;
        }).thenCompose(GraphExecutor::allOf);
        return this.associateResultsWithConnections(node, resultsByConnectorNameFuture);
    }

    /**
     * Creates future of a map of connections to results
     * from a map of connector names to results.
     *
     * @param node                         the node
     * @param resultsByConnectorNameFuture a future of the outputs of the node by connector name
     * @return the outputs of the node by connection
     */
    private CompletableFuture<Map<NodeConnection<?>, DataBox<?>>> associateResultsWithConnections(
        Node node,
        CompletableFuture<Map<String, DataBox<?>>> resultsByConnectorNameFuture
    ) {
        return resultsByConnectorNameFuture.thenApply(resultsByConnectorName -> {
            Map<NodeConnection<?>, DataBox<?>> resultsByConnection = new HashMap<>();
            for (var entry : resultsByConnectorName.entrySet()) {
                for (var conn : this.getOutputConnectionsByName(node, entry.getKey())) {
                    resultsByConnection.put(conn, entry.getValue());
                }
            }
            return resultsByConnection;
        });
    }

    /**
     * Creates an iterable of all connections associated with an output connector on
     * a node.
     *
     * @param node the node
     * @param name the name of the output connector on the node
     * @return an iterable of all connections from that output connector
     */
    private Iterable<NodeConnection<?>> getOutputConnectionsByName(Node node, String name) {
        return node.getOutputWithName(name).stream()
            .flatMap(c -> this.connections.stream().filter(a -> c.isCompatible(a.from())))
            .toList();
    }

    private static <K, V> CompletableFuture<Map<K, V>> allOf(Map<K, CompletableFuture<V>> futuresMap) {
        var allFuturesResult = CompletableFuture.allOf(futuresMap.values().toArray(CompletableFuture[]::new));
        return allFuturesResult.thenApply($ -> futuresMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().join()))
        );
    }
}
