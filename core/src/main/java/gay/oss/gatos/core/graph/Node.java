package gay.oss.gatos.core.graph;

import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Represents a node in a flow graph.
 * <p>Note that this class is entirely immutable - to modify a node in a graph, replace it with a new version.
 * This can be done with {@link Graph#modifyNode(UUID, UnaryOperator)}.</p>
 */
public final class Node {
    private final UUID id;
    private final NodeType type;
    private final @Unmodifiable Map<String, NodeSetting<?>> settings;
    private final @Unmodifiable Set<NodeConnector.Input<?>> inputs;
    private final @Unmodifiable Set<NodeConnector.Output<?>> outputs;

    private Node(
            UUID id,
            NodeType type,
            @Unmodifiable Map<String, NodeSetting<?>> settings,
            @Unmodifiable Set<NodeConnector.Input<?>> inputs,
            @Unmodifiable Set<NodeConnector.Output<?>> outputs) {
        this.id = id;
        this.type = type;
        this.settings = settings;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Create a new node with a given node type.
     * @param type  the node type
     * @return      the new node
     */
    public static Node create(NodeType type) {
        var defaultSettings = type.settings();
        var id = UUID.randomUUID();
        return new Node(
                id,
                type,
                defaultSettings,
                type.inputs(id, defaultSettings),
                type.outputs(id, defaultSettings)
        );
    }

    /**
     * Create a new node, the same as this one, but with a modified setting.
     * @param settingKey    the key of the setting to modify
     * @param value         the new value of the setting
     * @return              the new node
     * @param <T>           the type of the setting to modify
     */
    public <T> Node modifySetting(String settingKey, T value) {
        var setting = this.settings.get(settingKey);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting "+settingKey);
        }

        if (!setting.getTypeClass().isInstance(value)) {
            throw new IllegalArgumentException("Setting "+settingKey+" is not of type "+value.getClass().getName());
        }

        var newSettings = new HashMap<>(this.settings);

        // We know this cast succeeds because of the check above
        //noinspection unchecked
        newSettings.put(settingKey, ((NodeSetting<T>) setting).withValue(value));

        return new Node(
                this.id,
                this.type,
                newSettings,
                this.type.inputs(this.id, newSettings),
                this.type.outputs(this.id, newSettings)
        );
    }

    /**
     * Returns the UUID of this node.
     * @return  the UUID of this node
     */
    public UUID id() {
        return this.id;
    }

    /**
     * Returns the type of this node.
     * @return  the type of this node
     */
    public NodeType type() {
        return this.type;
    }

    /**
     * Returns the settings of this node.
     *
     * @return the settings of this node
     */
    public @Unmodifiable Map<String, NodeSetting<?>> settings() {
        return this.settings;
    }

    /**
     * Retrieve a setting for a given key.
     * @param key   the setting key
     * @param clazz the class of the setting
     * @return      the setting
     * @param <T>   the type of the setting
     * @throws IllegalArgumentException if the node does not contain a setting with the given key
     * @throws IllegalArgumentException if the setting with the given key does not hold data of the expected type
     */
    public <T> NodeSetting<T> getSetting(String key, Class<T> clazz) {
        var setting = this.settings.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting "+key);
        }

        if (!setting.getTypeClass().isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Setting "+key+" is not of type "+clazz.getName());
        }

        // We know this cast succeeds because of the check above
        //noinspection unchecked
        return (NodeSetting<T>) setting;
    }

    /**
     * Returns the inputs of this node.
     * @return  the inputs of this node
     */
    public @Unmodifiable Set<NodeConnector.Input<?>> inputs() {
        return this.inputs;
    }

    /**
     * Returns the outputs of this node.
     * @return  the outputs of this node
     */
    public @Unmodifiable Set<NodeConnector.Output<?>> outputs() {
        return this.outputs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Node) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.settings, that.settings) &&
                Objects.equals(this.inputs, that.inputs) &&
                Objects.equals(this.outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.type, this.settings, this.inputs, this.outputs);
    }

    @Override
    public String toString() {
        return "Node[" +
                "id=" + this.id + ", " +
                "type=" + this.type + ", " +
                "settings=" + this.settings + ", " +
                "inputs=" + this.inputs + ", " +
                "outputs=" + this.outputs + ']';
    }
}
