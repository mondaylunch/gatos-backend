package club.mondaylunch.gatos.core.graph.connector;

import java.util.Optional;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.DataTypeConversions;
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
        if (!DataTypeConversions.canConvert(from.type(), to.type()) || !to.type().equals(type)) {
            return Optional.empty();
        }

        // We know this cast succeeds because of the check above
        // noinspection unchecked
        var conn = new NodeConnection<T>(from.withType(type), (NodeConnector.Input<T>) to);
        return Optional.of(conn);
    }
}
