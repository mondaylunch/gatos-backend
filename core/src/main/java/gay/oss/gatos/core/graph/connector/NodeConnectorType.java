package gay.oss.gatos.core.graph.connector;

/**
 * Signifies a type of data which can be sent through a node connector.
 * @param clazz the class of the data
 * @param <T>   the type of the data
 */
public record NodeConnectorType<T>(Class<T> clazz) {
}
