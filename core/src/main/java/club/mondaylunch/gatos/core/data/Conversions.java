package club.mondaylunch.gatos.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.graph.MutableValueGraph;
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
    private static final Map<ConversionPair, Function<?, ?>> MAP = new HashMap<>();
    private static final MutableValueGraph<DataType<?>, Function<?, ?>> TYPE_CONVERSIONS = ValueGraphBuilder
        .directed()
        .allowsSelfLoops(true)
        .build();

    /**
     * Register a conversion between two types.
     *
     * @param typeA              the first DataType
     * @param typeB              the second DataType
     * @param conversionFunction a function to convert from A to B
     * @param <A>                the first type
     * @param <B>                the second type
     */
    public static <A, B> void register(DataType<A> typeA, DataType<B> typeB, Function<A, B> conversionFunction) {
        MAP.put(new ConversionPair(typeA, typeB), conversionFunction);
        TYPE_CONVERSIONS.putEdgeValue(typeA, typeB, conversionFunction);
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
        } else if (TYPE_CONVERSIONS.hasEdgeConnecting(a, b)) {
            return true;
        } else {
            var traverser = Traverser.forGraph(TYPE_CONVERSIONS);
            var nodePath = traverser.depthFirstPreOrder(a);
            for (var node : nodePath) {
                if (node.equals(b)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Convert a DataBox of one type to another.
     *
     * @param a     the DataBox to convert
     * @param typeB the DataType to convert to
     * @param <A>   the first type
     * @param <B>   the second type
     * @return the converted DataBox
     */
    @SuppressWarnings("unchecked")
    public static <A, B> DataBox<B> convert(DataBox<A> a, DataType<B> typeB) {
        if (a.type().equals(typeB)) {
            return (DataBox<B>) a;
        }
        var func = getConversionFunction(a.type(), typeB)
            .orElseThrow(() -> new ConversionException("Cannot convert %s to %s".formatted(a.type(), typeB)));
        B result = func.apply(a.value());
        if (result == null) {
            throw new ConversionException("Conversion function %s -> %s on %s returned null!".formatted(a.type(), typeB, a.value()));
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
        } else if (TYPE_CONVERSIONS.hasEdgeConnecting(a, b)) {
            return (Optional<Function<A, B>>) (Optional<?>) TYPE_CONVERSIONS.edgeValue(a, b);
        } else {
            return getPath(TYPE_CONVERSIONS, a, b).flatMap(conversions -> conversions.stream()
                .map(function -> (Function<Object, Object>) function)
                .reduce(Function::andThen)
                .map(function -> (Function<A, B>) function)
            );
        }
    }

    /**
     * Get the path between two nodes in a graph.
     *
     * @param graph The graph.
     * @param start The start node.
     * @param end   The end node.
     * @param <N>   The type of the nodes.
     * @param <V>   The type of the edges.
     * @return An {@code Optional} describing the path.
     * If there is no path, the {@code Optional} will be empty.
     */
    @SuppressWarnings("SameParameterValue")
    private static <N, V> Optional<List<V>> getPath(ValueGraph<N, V> graph, N start, N end) {
        var traverser = Traverser.forGraph(graph);
        var nodePaths = traverser.depthFirstPreOrder(start);
        List<V> edgePath = new ArrayList<>();
        var found = false;
        var currentNode = start;
        for (var node : nodePaths) {
            /*
            The first node in the iterable is
            the start node, so we skip it.
             */
            if (!node.equals(start)) {
                /*
                If the node is next to the start node,
                it means we're going down a new path.
                 */
                if (graph.hasEdgeConnecting(start, node)) {
                    edgePath.clear();
                    currentNode = start;
                }

                /*
                Add the edge to the path between the
                nodes to the path.
                 */
                var finalCurrentNode = currentNode;
                var edge = graph.edgeValue(currentNode, node).orElseThrow(
                    () -> new IllegalStateException(
                        "No edge found between %s and %s".formatted(finalCurrentNode, node)
                    )
                );
                edgePath.add(edge);

                /*
                If the node is the same as the end node,
                we've found a path.
                 */
                if (node.equals(end)) {
                    found = true;
                    break;
                } else {
                    currentNode = node;
                }
            }
        }
        if (found) {
            return Optional.of(edgePath);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get a list of all registered conversions.
     *
     * @return a list of all registered conversions
     */
    public static List<ConversionPair> getAllConversions() {
        return List.copyOf(MAP.keySet());
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

        public ConversionException(Throwable cause) {
            super(cause);
        }
    }
}
