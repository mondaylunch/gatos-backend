package club.mondaylunch.gatos.core.executor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
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
        this(graph.getExecutionOrder().orElseThrow(IllegalArgumentException::new), graph.getConnections());
    }

    /**
     * Creates a function which, when run, executes this flow graph from a certain input node using a given input.
     * @param triggerNodeId the UUID of the start node that should take in the input
     * @param <T>           the type of the input
     * @return              an execution function
     */
    public <T> Consumer<@Nullable T> execute(@Nullable UUID triggerNodeId) {
        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> results = new ConcurrentHashMap<>();

        final Node triggerNode;
        if (triggerNodeId != null) {
            triggerNode = this.startNodes.stream().filter(n -> n.id().equals(triggerNodeId)).findFirst().orElseThrow();
        } else {
            triggerNode = null;
        }

        return input -> {
            for (var node : this.nonEndNodes) {
                if (node == triggerNode) {
                    @SuppressWarnings("unchecked") Map<String, CompletableFuture<DataBox<?>>> outputs
                        = (((NodeType.Start<T>) node.type()).compute(input, node.settings()));
                    results.putAll(this.associateResultsWithConnections(node, outputs));
                } else {
                    var inputs = this.collectInputsForNode(node, results);
                    var res = this.getNodeResults(node, inputs);
                    results.putAll(res);
                }
            }

            CompletableFuture.allOf(this.endNodes.stream().map(node -> {
                var inputs = this.collectInputsForNode(node, results);
                return ((NodeType.End) node.type()).compute(inputs, node.settings());
            }).toArray(CompletableFuture[]::new)).join();
        };
    }

    /**
     * Convenience overload of {@link #execute(UUID)} which returns a Runnable instead, and triggers no start node.
     * @return a Runnable which, when run, executes this flow graph
     */
    public Runnable execute() {
        Consumer<@Nullable Object> func = this.execute(null);
        return () -> func.accept(null);
    }

    /**
     * Waits for the dependencies of a node to be completed, then returns them in a
     * map associated by their input
     * connector name.
     * @param node       the node
     * @param allResults the map of node connection results, for retrieving input
     *                   values from
     * @return the input values, associated by connector name
     */
    private Map<String, DataBox<?>> collectInputsForNode(
            Node node,
            Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> allResults) {
        Map<String, DataBox<?>> inputs = new HashMap<>();
        for (var dep : this.nodeDependencies.get(node)) {
            var resForDep = allResults.get(dep);
            var depInputName = dep.to().name();
            inputs.put(depInputName, Conversions.convert(resForDep.join(), dep.to().type()));
        }

        return inputs;
    }

    /**
     * Creates a map of each of a node's output connections to a CompletableFuture
     * of their output data.
     * @param node   the node
     * @param inputs a map of inputs to the node
     * @return a map from each output connection to that connection's output
     */
    private Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> getNodeResults(Node node,
            Map<String, DataBox<?>> inputs) {
        Map<String, CompletableFuture<DataBox<?>>> resultsByConnectorName = node
                .type() instanceof NodeType.WithOutputs outputs
                        ? outputs.compute(inputs, node.settings(), node.inputTypes())
                        : Map.of();
        return this.associateResultsWithConnections(node, resultsByConnectorName);
    }

    /**
     * Creates a map of connections to results from a map of connector names to results.
     * @param node                      the node
     * @param resultsByConnectorName    the outputs of the node by connector name
     * @return                          the outputs of the node by connection
     */
    private Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> associateResultsWithConnections(
        Node node,
        Map<String, CompletableFuture<DataBox<?>>> resultsByConnectorName) {

        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> resultsByConnection = new HashMap<>();
        for (var entry : resultsByConnectorName.entrySet()) {
            for (var conn : this.getOutputConnectionsByName(node, entry.getKey())) {
                resultsByConnection.put(conn, entry.getValue());
            }
        }

        return resultsByConnection;
    }

    /**
     * Creates an iterable of all connections associated with an output connector on
     * a node.
     * @param node the node
     * @param name the name of the output connector on the node
     * @return an iterable of all connections from that output connector
     */
    private Iterable<NodeConnection<?>> getOutputConnectionsByName(Node node, String name) {
        return node.getOutputWithName(name).stream()
                .flatMap(c -> this.connections.stream().filter(a -> c.isCompatible(a.from())))
                .toList();
    }
}
