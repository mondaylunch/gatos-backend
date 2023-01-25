package gay.oss.gatos.core.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Unmodifiable;

import gay.oss.gatos.core.graph.Node;
import gay.oss.gatos.core.graph.NodeCategory;
import gay.oss.gatos.core.graph.connector.NodeConnection;
import gay.oss.gatos.core.graph.data.DataBox;

//TODO cleanup
public class GraphExecutor {
    private final @Unmodifiable List<Node> nodes;
    private final @Unmodifiable Collection<Node> outputNodes;
    private final @Unmodifiable Collection<NodeConnection<?>> connections;
    private final @Unmodifiable Map<Node, Collection<NodeConnection<?>>> nodeDependencies;

    public GraphExecutor(List<Node> nodes, Set<NodeConnection<?>> connections) {
        this.nodes = List.copyOf(nodes);
        this.outputNodes = nodes.stream().filter(n -> n.type().category() == NodeCategory.OUTPUT).toList();
        this.connections = List.copyOf(connections);
        this.nodeDependencies = this.nodes.stream().collect(Collectors.toMap(
            Function.identity(),
            n -> n.inputs().values().stream().flatMap(connector -> this.connections.stream().filter(connection -> connection.to().equals(connector))).toList()
        ));
    }

    public Runnable execute() {
        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> results = new ConcurrentHashMap<>();

        return () -> {
            for (var node : this.nodes) {
                var res = this.resultOfNode(node, results);
                results.putAll(res);
            }
        };
    }

    private Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> resultOfNode(
        Node node,
        Map<NodeConnection<?>, CompletableFuture<DataBox<?>>> allResults
    ) {
        Map<String, DataBox<?>> inputs = new HashMap<>();
        for (var dep : this.nodeDependencies.get(node)) {
            var resForDep = allResults.get(dep);
            var depInputName = dep.to().name();
            inputs.put(depInputName, resForDep.join());
        }

        return node.type().compute(inputs, node.settings()).entrySet().stream().collect(Collectors.toMap(e -> this.getOutputConnectionByName(node, e.getKey()), Map.Entry::getValue));
    }

    private NodeConnection<?> getOutputConnectionByName(Node node, String name) {
        return node.getOutputWithName(name).flatMap(c -> this.connections.stream().filter(a -> a.from().equals(c)).findAny()).orElseThrow();
    }
}
