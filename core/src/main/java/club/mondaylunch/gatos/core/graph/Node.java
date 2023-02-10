package club.mondaylunch.gatos.core.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * Represents a node in a flow graph.
 * <p>
 * Note that this class is entirely immutable - to modify a node in a graph,
 * replace it with a new version.
 * This can be done with {@link Graph#modifyNode(UUID, UnaryOperator)}.
 * </p>
 */
public final class Node {

    private final UUID id;
    private final NodeType type;
    private final @Unmodifiable Map<String, DataBox<?>> settings;
    private final @Unmodifiable Map<String, NodeConnector.Input<?>> inputs;
    private final @Unmodifiable Map<String, NodeConnector.Output<?>> outputs;

    private Node(
            UUID id,
            NodeType type,
            Map<String, DataBox<?>> settings,
            Set<NodeConnector.Input<?>> inputs,
            Set<NodeConnector.Output<?>> outputs) {
        this.id = id;
        this.type = type;
        this.settings = Map.copyOf(settings);
        this.inputs = Map.copyOf(inputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
        this.outputs = Map.copyOf(outputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
    }

    /**
     * Needed for deserialization.
     */
    @ApiStatus.Internal
    public Node() {
        this.id = null;
        this.type = null;
        this.settings = null;
        this.inputs = null;
        this.outputs = null;
    }

    /**
     * Create a new node with a given node type.
     * @param type the node type
     * @return the new node
     */
    public static Node create(NodeType type) {
        var defaultSettings = type.settings();
        var id = UUID.randomUUID();
        return new Node(
                id,
                type,
                defaultSettings,
                NodeType.inputsOrEmpty(type, id, defaultSettings),
                NodeType.outputsOrEmpty(type, id, defaultSettings));
    }

    /**
     * Create a new node, the same as this one, but with a modified setting.
     * @param settingKey the key of the setting to modify
     * @param value      the new value of the setting
     * @param <T>        the type of the setting to modify
     * @return the new node
     */
    public <T> Node modifySetting(String settingKey, DataBox<T> value) {
        var setting = this.settings.get(settingKey);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting " + settingKey);
        }

        if (!setting.type().equals(value.type())) {
            throw new IllegalArgumentException("Setting " + settingKey + " is not of type " + value.type().name());
        }

        var newSettings = new HashMap<>(this.settings);

        newSettings.put(settingKey, value);

        return new Node(
                this.id,
                this.type,
                newSettings,
                NodeType.inputsOrEmpty(this.type, this.id, newSettings),
                NodeType.outputsOrEmpty(this.type, this.id, newSettings));
    }

    /**
     * Returns the UUID of this node.
     * @return the UUID of this node
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Returns the type of this node.
     * @return the type of this node
     */
    public NodeType getType() {
        return this.type;
    }

    /**
     * Returns the settings of this node.
     *
     * @return the settings of this node
     */
    public @Unmodifiable Map<String, DataBox<?>> getSettings() {
        return this.settings;
    }

    /**
     * Retrieve a setting for a given key.
     * @param key  the setting key
     * @param type the datatype of the setting
     * @param <T>  the type of the setting
     * @return the setting
     * @throws IllegalArgumentException if the node does not contain a setting with
     *                                  the given key
     * @throws IllegalArgumentException if the setting with the given key does not
     *                                  hold data of the expected type
     */
    public <T> DataBox<T> getSetting(String key, DataType<T> type) {
        var setting = this.settings.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting " + key);
        }

        if (!setting.type().equals(type)) {
            throw new IllegalArgumentException("Setting " + key + " is not of type " + type.name());
        }

        // We know this cast succeeds because of the check above
        // noinspection unchecked
        return (DataBox<T>) setting;
    }

    /**
     * Returns the inputs of this node.
     * @return the inputs of this node
     */
    public @Unmodifiable Map<String, NodeConnector.Input<?>> getInputs() {
        return this.inputs;
    }

    /**
     * Possibly retrieves an input connector with the given name.
     * @param name the name of the connector
     * @return an Optional of the connector
     */
    public Optional<NodeConnector.Input<?>> getInputWithName(String name) {
        return Optional.ofNullable(this.inputs.get(name));
    }

    /**
     * Returns the outputs of this node.
     * @return the outputs of this node
     */
    public @Unmodifiable Map<String, NodeConnector.Output<?>> getOutputs() {
        return this.outputs;
    }

    /**
     * Possibly retrieves an output connector with the given name.
     * @param name the name of the connector
     * @return an Optional of the connector
     */
    public Optional<NodeConnector.Output<?>> getOutputWithName(String name) {
        return Optional.ofNullable(this.outputs.get(name));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        var that = (Node) obj;
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.settings, that.settings)
                && Objects.equals(this.inputs, that.inputs)
                && Objects.equals(this.outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.type, this.settings, this.inputs, this.outputs);
    }

    @Override
    public String toString() {
        return "Node[id=%s, type=%s, settings=%s, inputs=%s, outputs=%s]".formatted(
                this.id,
                this.type,
                this.settings,
                this.inputs,
                this.outputs);
    }
}
