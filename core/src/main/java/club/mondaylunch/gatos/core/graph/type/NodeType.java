package club.mondaylunch.gatos.core.graph.type;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.Registry;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.models.Flow;

/**
 * A type of node that may go on a graph.
 *
 * <p>
 * A new node type is created by extending one of {@link NodeType.Start},
 * {@link NodeType.Process}, or {@link NodeType.End}.
 * The differences between these are explained in {@link NodeCategory}.
 * </p>
 *
 * <p>
 * A node type specifies its settings, which are always the same; and its inputs
 * and outputs, which vary depending on
 * its settings.
 * </p>
 *
 * <p>
 * Depending on its category, a node type may have to specify a compute
 * function. This takes in a nodes inputs and settings,
 * and computes its outputs (or, for an End node, Void).
 * </p>
 */
@ApiStatus.NonExtendable
public interface NodeType {
    /**
     * The Node Type registry.
     */
    Registry<NodeType> REGISTRY = Registry.create("node_type", NodeType.class);

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
     * Determine whether the given node is valid in the graph.
     * @param node  the node to check
     * @param graph the graph the node is in
     * @return      any errors related to the node in the graph
     */
    Collection<GraphValidityError> isValid(Node node, Graph graph);

    /**
     * Extended by node types which have input connectors.
     */
    @ApiStatus.NonExtendable
    interface WithInputs {
        /**
         * The input connectors of a node with a given UUID, settings, & current input connections (if any).
         * @param nodeId the node UUID
         * @param settings  the node settings
         * @param inputTypes what type of output connector the input connectors to this node are connected to, if any
         * @return the input connectors of the node
         */
        Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes);
    }

    /**
     * Extended by node types which have output connectors.
     */
    @ApiStatus.NonExtendable
    interface WithOutputs {
        /**
         * The output connectors of a node with a given UUID, settings, & input connections.
         * @param nodeId the node UUID
         * @param settings  the node settings
         * @param inputTypes what type of output connector the input connectors to this node are connected to, if any
         * @return the output connectors of the node
         */
        Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes);

        /**
         * (Asynchronously) compute the outputs of this node in a map of output
         * connector name to value.
         * @param inputs   a map of input connector name to value
         * @param settings a map of node settings
         * @param inputTypes what type of output connector the input connectors to this node are connected to, if any
         * @return a CompletableFuture of each output in a map by name
         */
        Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
                Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes);
    }

    /**
     * A node type of the {@link NodeCategory#START start} category.
     */
    abstract class Start<StartInput> implements NodeType, WithOutputs {
        @Override
        public final NodeCategory category() {
            return NodeCategory.START;
        }

        @Override
        public Collection<GraphValidityError> isValid(Node node, Graph graph) {
            return Set.of();
        }

        /**
         * Perform whatever setup is needed to get the flow this node is in to trigger.
         * @param flow  the flow this node is a part of
         * @param function the function to call to start the flow from this node
         * @param node  the node
         */
        public abstract void setupFlow(Flow flow, Consumer<@Nullable StartInput> function, Node node);

        /**
         * Perform whatever teardown is needed to make this flow no longer trigger from this node.
         * @param flow the flow this node is a part of
         * @param node the node (pre-whatever modification made it invalid)
         */
        public abstract void teardownFlow(Flow flow, Node node);

        /**
         * (Asynchronously) compute the outputs of this node in a map of output
         * connector name to value.
         * @param startInput whatever input this start node has. This can be null, if this node is not the one triggering the flow!
         * @param settings a map of node settings
         * @return a CompletableFuture of each output in a map by name
         */
        public abstract Map<String, CompletableFuture<DataBox<?>>> compute(@Nullable StartInput startInput, Map<String, DataBox<?>> settings);

        @Override
        public final Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
            return this.compute(null, settings);
        }
    }

    /**
     * A node type of the {@link NodeCategory#PROCESS process} category.
     */
    abstract class Process implements NodeType, WithInputs, WithOutputs {
        @Override
        public Collection<GraphValidityError> isValid(Node node, Graph graph) {
            return defaultValidation(node, graph);
        }

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
        public Collection<GraphValidityError> isValid(Node node, Graph graph) {
            return NodeType.defaultValidation(node, graph);
        }

        @Override
        public final NodeCategory category() {
            return NodeCategory.END;
        }

        /**
         * (Asynchronously) compute this node.
         * @param inputs   a map of input connector name to value
         * @param settings a map of node settings
         * @return a CompletableFuture of this node's computation
         */
        public abstract CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs,
                Map<String, DataBox<?>> settings);
    }

    /**
     * Gets the inputs of a node type, or an empty set if the type does not specify
     * inputs.
     * @param type  the node type
     * @param id    the UUID of the node these inputs are for
     * @param settings the settings of the node these inputs are for
     * @return the inputs, or empty
     */
    static Set<NodeConnector.Input<?>> inputsOrEmpty(NodeType type, UUID id, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return type instanceof WithInputs inputType ? inputType.inputs(id, settings, inputTypes) : Set.of();
    }

    /**
     * Gets the outputs of a node type, or an empty set if the type does not specify
     * outputs.
     * @param type  the node type
     * @param id    the UUID of the node these outputs are for
     * @param settings the settings of the node these outputs are for
     * @param inputTypes what type of output connector the input connectors to the node are connected to, if any
     * @return the outputs, or empty
     */
    static Set<NodeConnector.Output<?>> outputsOrEmpty(NodeType type, UUID id, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return type instanceof WithOutputs outputType ? outputType.outputs(id, settings, inputTypes) : Set.of();
    }

    /**
     * Ensures that all inputs of a node are hooked up, if any outputs are.
     * @param node  the node
     * @param graph the graph
     * @return      any validation errors
     */
    @NotNull
    static Collection<GraphValidityError> defaultValidation(Node node, Graph graph) {
        var conns = List.copyOf(graph.getConnectionsForNode(node.id()));
        if (conns.stream().anyMatch(c -> c.to().nodeId().equals(node.id()))) {
            return node.inputs().values().stream()
                .filter(input -> conns.stream().noneMatch(c -> c.to().equals(input)))
                .map(GraphValidityError::missingInput)
                .toList();
        } else {
            return List.of();
        }
    }
}
