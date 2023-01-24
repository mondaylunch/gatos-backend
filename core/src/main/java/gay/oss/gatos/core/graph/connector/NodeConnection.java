package gay.oss.gatos.core.graph.connector;

/**
 * A connection from an output connector on one node to an input connector on another.
 * @param from  the output connector this connection is from
 * @param to    the input connector this connection is to
 * @param <T>   the type of connector this connection connects
 */
public record NodeConnection<T extends NodeConnectorType<?>>(
        NodeConnector.Output<T> from,
        NodeConnector.Input<T> to) {
}
