package gay.oss.gatos.core.graph.type;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;

import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.data.DataBox;

/**
 * A type of node that may go on a graph.
 *
 * <p>A new node type is created by extending one of {@link NodeType.Start}, {@link NodeType.Process}, or {@link NodeType.End}.
 * The differences between these are explained in {@link NodeCategory}.</p>
 *
 * <p>A node type specifies its settings, which are always the same; and its inputs and outputs, which vary depending on
 * its settings.</p>
 *
 * <p>Depending on its category, a node type may have to specify a compute function. This takes in a nodes inputs and settings,
 * and computes its outputs (or, for an End node, Void).</p>
 */
@ApiStatus.NonExtendable
public interface NodeType {
    /**
     * Returns the category this node type belongs to.
     * @return the category this node type belongs to
     */
    NodeCategory category();

    /**
     * The setting keys and default values for a node of this type.
     * @return the settings for a node of this type
     */
    Map<String, DataBox<?>> settings();

    /**
     * Extended by node types which have input connectors.
     */
    @ApiStatus.NonExtendable
    interface WithInputs {
        /**
         * The input connectors of a node with a given UUID & settings state.
         * @param nodeId    the node UUID
         * @param state     the node settings
         * @return          the input connectors of the node
         */
        Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state);
    }

    /**
     * Extended by node types which have output connectors.
     */
    @ApiStatus.NonExtendable
    interface WithOutputs {
        /**
         * The output connectors of a node with a given UUID & settings state.
         * @param nodeId    the node UUID
         * @param state     the node settings
         * @return          the output connectors of the node
         */
        Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state);

        /**
         * (Asynchronously) compute the outputs of this node in a map of output connector name to value.
         * @param inputs    a map of input connector name to value
         * @param settings  a map of node settings
         * @return          a CompletableFuture of each output in a map by name
         */
        Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings);
    }

    /**
     * A node type of the {@link NodeCategory#START start} category.
     */
    abstract class Start implements NodeType, WithOutputs {
        @Override
        public final NodeCategory category() {
            return NodeCategory.START;
        }
    }

    /**
     * A node type of the {@link NodeCategory#PROCESS process} category.
     */
    abstract class Process implements NodeType, WithInputs, WithOutputs {
        @Override
        public final NodeCategory category() {
            return NodeCategory.PROCESS;
        }
    }

    /**
     * A node type of the {@link NodeCategory#END end} category.
     */
    abstract class End implements NodeType, WithInputs {
        @Override
        public final NodeCategory category() {
            return NodeCategory.END;
        }

        /**
         * (Asynchronously) compute this node.
         * @param inputs    a map of input connector name to value
         * @param settings  a map of node settings
         * @return          a CompletableFuture of this node's computation
         */
        public abstract CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings);
    }

    /**
     * Gets the inputs of a node type, or an empty set if the type does not specify inputs.
     * @param type  the node type
     * @param id    the UUID of the node these inputs are for
     * @param state the settings state of the node these inputs are for
     * @return      the inputs, or empty
     */
    static Set<NodeConnector.Input<?>> inputsOrEmpty(NodeType type, UUID id, Map<String, DataBox<?>> state) {
        return type instanceof WithInputs inputType ? inputType.inputs(id, state) : Set.of();
    }

    /**
     * Gets the outputs of a node type, or an empty set if the type does not specify outputs.
     * @param type  the node type
     * @param id    the UUID of the node these outputs are for
     * @param state the settings state of the node these outputs are for
     * @return      the outputs, or empty
     */
    static Set<NodeConnector.Output<?>> outputsOrEmpty(NodeType type, UUID id, Map<String, DataBox<?>> state) {
        return type instanceof WithOutputs outputType ? outputType.outputs(id, state) : Set.of();
    }
}
