package gay.oss.gatos.core.graph;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;

/**
 * A type of node that may go on a graph.
 */
public interface NodeType {
    /**
     * Returns the category this node type belongs to.
     * @return the category this node type belongs to
     */
    NodeCategory category();

    /**
     * The input connectors of a node with a given UUID & settings state.
     * @param nodeId    the node UUID
     * @param state     the node settings
     * @return          the input connectors of the node
     */
    Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state);

    /**
     * The output connectors of a node with a given UUID & settings state.
     * @param nodeId    the node UUID
     * @param state     the node settings
     * @return          the output connectors of the node
     */
    Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state);

    /**
     * The setting keys and default values for a node of this type.
     * @return the settings for a node of this type
     */
    Map<String, DataBox<?>> settings();

    /**
     * (Asynchronously) compute the outputs of this node in a map of output connector name to value.
     * @param inputs    a map of input connector name to value
     * @param settings  a map of node settings
     * @return          a CompletableFuture of each output in a map by name
     */
    Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings);
}
