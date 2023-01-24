package gay.oss.gatos.core.graph;

import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;
import gay.oss.gatos.core.graph.data.DataType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A type of node that may go on a graph.
 */
public interface NodeType {
    /**
     * Whether nodes of this type can have inputs from other nodes. If not, then this is an <i>input node</i> type.
     * @return whether nodes of this type can have inputs.
     */
    public boolean hasInputs();

    /**
     * Whether nodes of this type can have outputs to other nodes. If not, then this is a <i>terminal node</i> type.
     * @return whether nodes of this type can have outputs.
     */
    public boolean hasOutputs();

    /**
     * The input connectors of a node with a given UUID & settings state.
     * @param nodeId    the node UUID
     * @param state     the node settings
     * @return          the input connectors of the node
     */
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state);

    /**
     * The output connectors of a node with a given UUID & settings state.
     * @param nodeId    the node UUID
     * @param state     the node settings
     * @return          the output connectors of the node
     */
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state);

    /**
     * The setting keys and default values for a node of this type.
     * @return the settings for a node of this type
     */
    public Map<String, DataBox<?>> settings();
}