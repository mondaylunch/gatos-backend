package club.mondaylunch.gatos.core.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeCategory;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Handles execution of a {@link club.mondaylunch.gatos.core.graph.Graph flow
 * graph}.
 */
public class GraphExecutor {
    private final @Unmodifiable Collection<Node> nonEndNodes;
    private final @Unmodifiable Collection<Node> endNodes;
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
    }

    /**
     * Creates a Runnable which, when run, executes this flow graph.
     * @return an execution Runnable.
     */
    public Runnable execute() {
        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> results = new ConcurrentHashMap<>();

        return () -> {
            for (var node : this.nonEndNodes) {
                var inputs = this.collectInputsForNode(node, results);
                var res = this.getNodeResults(node, inputs);
                results.putAll(res);
            }
            CompletableFuture.allOf(this.endNodes.stream().map(node -> {
                var inputs = this.collectInputsForNode(node, results);
                return ((NodeType.End) node.type()).compute(inputs, node.settings());
            }).toArray(CompletableFuture[]::new)).join();
        };
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
        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> resultsByConnection = new HashMap<>();
        Map<String, CompletableFuture<DataBox<?>>> resultsByConnectorName = node
                .type() instanceof NodeType.WithOutputs outputs
                        ? outputs.compute(inputs, node.settings())
                        : Map.of();
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
                .flatMap(c -> this.connections.stream().filter(a -> a.from().isCompatible(c)))
                .toList();
    }
}
