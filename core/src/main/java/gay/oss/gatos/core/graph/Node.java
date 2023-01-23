package gay.oss.gatos.core.graph;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Node {
    private final UUID id;
    private final NodeType type;
    private final @Unmodifiable Set<NodeSetting<?>> settings;
    private final @Unmodifiable Set<NodeConnector.Input> inputs;
    private final @Unmodifiable Set<NodeConnector.Output> outputs;

    private Node(
            UUID id,
            NodeType type,
            @Unmodifiable Set<NodeSetting<?>> settings,
            @Unmodifiable Set<NodeConnector.Input> inputs,
            @Unmodifiable Set<NodeConnector.Output> outputs) {
        this.id = id;
        this.type = type;
        this.settings = settings;
        this.inputs = inputs;
        this.outputs = outputs;
    }

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

    public Node modifySetting() {

    }

    public UUID id() {
        return this.id;
    }

    public NodeType type() {
        return this.type;
    }

    public @Unmodifiable Set<NodeSetting<?>> settings() {
        return this.settings;
    }

    public @Unmodifiable Set<NodeConnector.Input> inputs() {
        return this.inputs;
    }

    public @Unmodifiable Set<NodeConnector.Output> outputs() {
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
