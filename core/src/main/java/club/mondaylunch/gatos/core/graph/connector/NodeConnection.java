package club.mondaylunch.gatos.core.graph.connector;

import java.util.Optional;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.graph.Node;

/**
 * A connection from an output connector on one node to an input connector on
 * another.
 * @param from the output connector this connection is from
 * @param to   the input connector this connection is to
 * @param <T>  the type of connector this connection connects
 */
public record NodeConnection<T>(
        NodeConnector.Output<T> from,
        NodeConnector.Input<T> to) {

    public static <T> Optional<NodeConnection<T>> createConnection(Node fromNode, String fromName, Node toNode,
            String toName, DataType<T> type) {
        var fromOpt = fromNode.getOutputWithName(fromName);
        var toOpt = toNode.getInputWithName(toName);
        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            return Optional.empty();
        }

        var from = fromOpt.get();
        var to = toOpt.get();
        if (!Conversions.canConvert(from.type(), to.type()) || !to.type().equals(type)) {
            return Optional.empty();
        }

        // We know this cast succeeds because of the check above
        // noinspection unchecked
        var conn = new NodeConnection<T>(from.withType(type), (NodeConnector.Input<T>) to);
        return Optional.of(conn);
    }

    public static NodeConnection<?> create(Node fromNode, String fromName, Node toNode, String toName) {
        var from = fromNode.getOutputWithName(fromName)
            .orElseThrow(() -> new IllegalArgumentException("Invalid node output: " + fromName));
        var to = toNode.getInputWithName(toName)
            .orElseThrow(() -> new IllegalArgumentException("Invalid node input: " + toNode));

        if (!Conversions.canConvert(from.type(), to.type())) {
            throw new IllegalArgumentException("Cannot convert type " + from.type() + " to type " + to.type());
        }

        @SuppressWarnings("unchecked")
        var connection = new NodeConnection<>(
            (NodeConnector.Output<Object>) from.withType(to.type()),
            (NodeConnector.Input<Object>) to
        );
        return connection;
    }
}
