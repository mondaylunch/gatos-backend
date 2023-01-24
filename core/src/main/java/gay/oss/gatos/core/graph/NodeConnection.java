package gay.oss.gatos.core.graph;

public record NodeConnection<T extends NodeConnectorType<?>>(
        NodeConnector.Output<T> from,
        NodeConnector.Input<T> to) {
}
