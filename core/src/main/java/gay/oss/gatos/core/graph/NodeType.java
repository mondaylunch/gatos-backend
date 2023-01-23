package gay.oss.gatos.core.graph;

import java.util.Set;

public interface NodeType {
    /**
     * Whether nodes of this type can have inputs from other nodes. If not, then this is an <i>input node</i> type.
     * @return whether nodes of this type can have inputs.
     */
    public boolean hasInputs();

    /**
     * Whether nodes of this type can have outputs to other nodes. If not, then this is a <i>terminal node</i>.type
     * @return whether nodes of this type can have outputs.
     */
    public boolean hasOutputs();

    /**
     * The input connectors of a node with a given settings state.
     * @param state the node settings
     * @return the input connectors of the node
     */
    public Set<NodeConnector.Input> inputs(Set<NodeSetting<?>> state);

    /**
     * The output connectors of a node with a given settings state.
     * @param state the node settings
     * @return the output connectors of the node
     */
    public Set<NodeConnector.Output> outputs(Set<NodeSetting<?>> state);

    /**
     * The settings available for a node of this type.
     * @return the settings available for a node of this type
     */
    public Set<NodeSetting<?>> settings();
}
