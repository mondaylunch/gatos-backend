package gay.oss.gatos.core.graph;

public record NodeConnection(
        NodeConnector.Output from,
        NodeConnector.Input to) {
}
