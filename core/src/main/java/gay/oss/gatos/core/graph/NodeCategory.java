package gay.oss.gatos.core.graph;

/**
 * A category a node can belong to.
 */
public enum NodeCategory {
    /**
     * A node at the 'start' of a flow, activated by the trigger. Every graph must have at least one of these.
     * May not have input connections. Must have output connections.
     */
    PUSHED_INPUT,
    /**
     * A regular node. May have input connections. Must have output connections.
     */
    PROCESS,
    /**
     * A conditional node. Takes in a boolean input.
     * Must have no output connections. Has condition-connections
     */
    CONDITIONAL,
    /**
     * An output node, at an end of the flow. Every graph must have at least one of these.
     * Must have input connections. May not have output connections.
     */
    OUTPUT
}
