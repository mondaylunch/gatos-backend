package club.mondaylunch.gatos.core.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;

/**
 * Manages conversions between {@link DataType DataTypes}. Conversions should only be registered in here if <strong>they will never fail</strong>.
 * <p>For example, Number -> String is a reasonable conversion, as all numbers can be represented as strings. String -> Number is not,
 * as not all strings are valid numbers.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class Conversions {

    /**
     * A directed graph of {@code DataType} conversions.
     * The nodes of the graph are the {@code DataType}s,
     * and the edges are the conversion functions between
     * {@code DataType}s.
     * {@code DataType}s are connected by an edge if there
     * is a direct conversion between them.
     */
    private static final MutableValueGraph<DataType<?>, Function<?, ?>> TYPE_CONVERSIONS = ValueGraphBuilder.directed()
        .allowsSelfLoops(true)
        .build();

    /**
     * Register a conversion between two types.
     * A conversion for {@link DataType#listOf() lists}
     * and {@link DataType#optionalOf() optionals}
     * of the two types will also be registered.
     *
     * @param typeA              the first DataType
     * @param typeB              the second DataType
     * @param conversionFunction a function to convert from A to B
     * @param <A>                the first type
     * @param <B>                the second type
     */
    public static <A, B> void register(DataType<A> typeA, DataType<B> typeB, Function<A, B> conversionFunction) {
        Function<List<A>, List<B>> listConversionFunction = elements -> convertList(elements, conversionFunction);
        Function<Optional<A>, Optional<B>> optionalConversionFunction = optional -> convertOptional(optional, conversionFunction);

        registerSimple(typeA, typeB, conversionFunction);
        registerSimple(typeA.listOf(), typeB.listOf(), listConversionFunction);
        registerSimple(typeA.optionalOf(), typeB.optionalOf(), optionalConversionFunction);
    }

    /**
     * Register a conversion between two types.
     *
     * @param typeA              the first DataType
     * @param typeB              the second DataType
     * @param conversionFunction a function to convert from A to B
     * @param <A>                the first type
     * @param <B>                the second type
     */
    public static <A, B> void registerSimple(DataType<A> typeA, DataType<B> typeB, Function<A, B> conversionFunction) {
        TYPE_CONVERSIONS.putEdgeValue(typeA, typeB, conversionFunction);
    }

    private static <A, B> List<B> convertList(List<A> elements, Function<A, B> conversionFunction) {
        return elements.stream()
            .map(conversionFunction)
            .toList();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <A, B> Optional<B> convertOptional(Optional<A> optional, Function<A, B> conversionFunction) {
        return optional.map(conversionFunction);
    }

    /**
     * Determines whether there is a conversion registered between two DataTypes.
     *
     * @param a the first DataType
     * @param b the second DataType
     * @return whether there is a conversion between the two
     */
    public static boolean canConvert(DataType<?> a, DataType<?> b) {
        if (a.equals(b)) {
            return true;
        }

        if (TYPE_CONVERSIONS.hasEdgeConnecting(a, b)) {
            return true;
        }

        if (TYPE_CONVERSIONS.nodes().contains(a) && TYPE_CONVERSIONS.nodes().contains(b)) {
            var traverser = Traverser.forGraph(TYPE_CONVERSIONS);
            var nodePath = traverser.depthFirstPreOrder(a);
            for (var node : nodePath) {
                if (node.equals(b)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <A, B> DataBox<B> convert(DataBox<A> a, DataType<B> typeB) {
        var valueA = a.value();
        if (valueA == null) {
            throw new ConversionException("Cannot convert null");
        }
        var func = getConversionFunction(a.type(), typeB)
            .orElseThrow(() -> new ConversionException("Cannot convert %s to %s".formatted(a.type(), typeB)));
        B result;
        try {
            result = func.apply(valueA);
        } catch (Exception e) {
            throw new ConversionException("Failed to convert %s to %s".formatted(a.type(), typeB), e);
        }
        if (result == null) {
            throw new ConversionException("Conversion function %s -> %s on %s returned null".formatted(a.type(), typeB, valueA));
        }

        return typeB.create(result);
    }

    /**
     * Gets the conversion function between two {@code DataType}s.
     *
     * @param a   The first {@code DataType}.
     * @param b   The second {@code DataType}.
     * @param <A> The type of the first {@code DataType}.
     * @param <B> The type of the second {@code DataType}.
     * @return An {@code Optional} containing the conversion function.
     * The {@code Optional} will be empty if no conversion path between
     * the two {@code DataType}s exists.
     */
    @SuppressWarnings("unchecked")
    private static <A, B> Optional<Function<A, B>> getConversionFunction(DataType<A> a, DataType<B> b) {
        if (a.equals(b)) {
            return Optional.of((Function<A, B>) Function.identity());
        }

        if (TYPE_CONVERSIONS.nodes().contains(a) && TYPE_CONVERSIONS.nodes().contains(b)) {
            return getPath(TYPE_CONVERSIONS, a, b).map(conversions -> conversions.stream()
                .map(Function.class::cast)
                .reduce(Function.identity(), Function::andThen)
            );
        }

        return Optional.empty();
    }

    /**
     * Get the edge path between two nodes in a graph.
     *
     * @param graph The graph.
     * @param start The start node.
     * @param end   The end node.
     * @param <N>   The type of the nodes.
     * @param <V>   The type of the edges.
     * @return An {@code Optional} describing the edge path.
     * If there is no path, the {@code Optional} will be empty.
     * @throws IllegalArgumentException if the {@code start} or {@code end} node is not in the graph.
     */
    @SuppressWarnings("SameParameterValue")
    private static <N, V> Optional<List<V>> getPath(ValueGraph<N, V> graph, N start, N end) {
        var graphNodes = graph.nodes();
        if (!graphNodes.contains(start)) {
            throw new IllegalArgumentException("Start node %s is not in the graph".formatted(start));
        }
        if (!graphNodes.contains(end)) {
            throw new IllegalArgumentException("End node %s is not in the graph".formatted(end));
        }

        /*
        If the start node is the end node,
        return an empty path.
         */
        if (start.equals(end)) {
            return Optional.of(List.of());
        }

        /*
        If there is an edge between the start
        and end nodes, return a path with that edge.
         */
        var edgeOptional = graph.edgeValue(start, end);
        if (edgeOptional.isPresent()) {
            return edgeOptional.map(List::of);
        }

        return getShortestPath(graph, start, end).map(nodePath -> getEdgePath(graph, nodePath));
    }

    private static <N, V> List<V> getEdgePath(ValueGraph<N, V> graph, List<N> nodePath) {
        if (nodePath.size() < 2) {
            return List.of();
        }

        List<V> edgePath = new ArrayList<>();
        for (int i = 0; i < nodePath.size() - 1; i++) {
            var node1 = nodePath.get(i);
            var node2 = nodePath.get(i + 1);
            var edge = graph.edgeValue(node1, node2)
                .orElseThrow(() -> new IllegalArgumentException("No edge found between %s and %s".formatted(node1, node2)));
            edgePath.add(edge);
        }
        return edgePath;
    }

    private static <N> Optional<List<N>> getShortestPath(SuccessorsFunction<N> graph, N start, N end) {
        return breadthFirstSearch(graph, start, end).map(predecessors -> getPath(end, predecessors));
    }

    private static <N> List<N> getPath(N end, Map<N, N> predecessors) {
        List<N> reversePath = new ArrayList<>();
        N crawl = end;
        reversePath.add(crawl);
        while (predecessors.containsKey(crawl)) {
            crawl = predecessors.get(crawl);
            reversePath.add(crawl);
        }
        return Lists.reverse(reversePath);
    }

    /**
     * A modified version of breadth first search that returns
     * predecessor of each node visited. The search starts from
     * the {@code start} node and ends when the {@code end} node
     * is found.
     *
     * @param graph The graph to search.
     * @param start The start node.
     * @param end   The end node.
     * @param <N>   The type of the nodes.
     * @return An {@code Optional} containing the predecessors map,
     * mapping a node to its predecessor. If there is no path between
     * the {@code start} and {@code end} nodes, the {@code Optional}
     * will be empty.
     */
    private static <N> Optional<Map<N, N>> breadthFirstSearch(SuccessorsFunction<N> graph, N start, N end) {
        Map<N, N> predecessors = new HashMap<>();

        Queue<N> nodeQueue = new ArrayDeque<>();
        Set<N> visitedNodes = new HashSet<>();

        visitedNodes.add(start);
        nodeQueue.add(start);

        while (!nodeQueue.isEmpty()) {
            N visiting = nodeQueue.remove();
            for (N adjacentNode : graph.successors(visiting)) {
                if (!visitedNodes.contains(adjacentNode)) {
                    visitedNodes.add(adjacentNode);
                    predecessors.put(adjacentNode, visiting);
                    nodeQueue.add(adjacentNode);

                    if (adjacentNode.equals(end)) {
                        return Optional.of(predecessors);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get a list of all registered conversions.
     *
     * @return a list of all registered conversions
     */
    public static List<ConversionPair> getAllConversions() {
        List<ConversionPair> conversions = new ArrayList<>();
        var traverser = Traverser.forGraph(TYPE_CONVERSIONS);
        for (var node : TYPE_CONVERSIONS.nodes()) {
            var nodePaths = traverser.depthFirstPreOrder(node);
            for (var connectedNode : nodePaths) {
                if (!connectedNode.equals(node)) {
                    conversions.add(new ConversionPair(node, connectedNode));
                }
            }
        }
        return conversions;
    }

    private Conversions() {
    }

    public record ConversionPair(DataType<?> a, DataType<?> b) {
    }

    /**
     * Thrown when there is an error in DataType conversion.
     */
    public static class ConversionException extends RuntimeException {

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
