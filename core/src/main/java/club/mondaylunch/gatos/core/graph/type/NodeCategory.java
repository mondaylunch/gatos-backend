package club.mondaylunch.gatos.core.graph.type;

/**
 * A category a node can belong to.
 */
public enum NodeCategory {
    /**
     * A node at the start of a flow, activated by the trigger. Every graph must
     * have at least one of these.
     * May not have input connections. Must have output connections.
     */
    START,
    /**
     * A regular node. May have input connections. Must have output connections.
     */
    PROCESS,
    /**
     * A conditional node. Takes in a boolean input.
     * Must have no output connections. Has condition-connections
     */
    @SuppressWarnings("unused")
    CONDITIONAL,
    /**
     * A node at an end of a flow. Every graph must have at least one of these.
     * Must have input connections. May not have output connections.
     */
    END
}
