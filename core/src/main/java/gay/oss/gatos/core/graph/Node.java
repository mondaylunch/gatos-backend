package gay.oss.gatos.core.graph;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Represents a node in a flow graph.
 * <p>Note that this class is entirely immutable - to modify a node in a graph, replace it with a new version.
 * This can be done with {@link Graph#modifyNode(UUID, UnaryOperator)}.</p>
 */
public final class Node {
    private final UUID id;
    private final NodeType type;
    private final @Unmodifiable Set<NodeSetting<?>> settings;
    private final @Unmodifiable Set<NodeConnector.Input<?>> inputs;
    private final @Unmodifiable Set<NodeConnector.Output<?>> outputs;

    private Node(
            UUID id,
            NodeType type,
            @Unmodifiable Set<NodeSetting<?>> settings,
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
        return new Node(
                UUID.randomUUID(),
                type,
                defaultSettings,
                type.inputs(defaultSettings),
                type.outputs(defaultSettings)
        );
    }

    /**
     * Create a new node, the same as this one, but with a modified setting.
     * @param setting   the setting to modify
     * @param value     the new value of the setting
     * @return          the new node
     * @param <T>       the type of the setting to modify
     */
    public <T> Node modifySetting(NodeSetting<T> setting, T value) {
        var newSettings = this.type.settings();
        newSettings.remove(setting);
        newSettings.add(setting.withValue(value));
        return new Node(
                this.id,
                this.type,
                newSettings,
                this.type.inputs(newSettings),
                this.type.outputs(newSettings)
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
     * @return  the settings of this node
     */
    public @Unmodifiable Set<NodeSetting<?>> settings() {
        return this.settings;
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
