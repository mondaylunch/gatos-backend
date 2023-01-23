package gay.oss.gatos.core.graph;

import java.util.Objects;
import java.util.UUID;

public sealed abstract class NodeConnector {
    private final NodeConnectorType type;
    private final UUID nodeId;
    private final String name;

    protected NodeConnector(NodeConnectorType type, UUID nodeId, String name) {
        this.type = type;
        this.nodeId = nodeId;
        this.name = name;
    }

    public NodeConnectorType type() {
        return this.type;
    }

    public UUID nodeId() {
        return this.nodeId;
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NodeConnector) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.nodeId, that.nodeId) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.name);
    }

    public static final class Input extends NodeConnector {
        public Input(NodeConnectorType type, UUID nodeId, String name) {
            super(type, nodeId, name);
        }
    }

    public static final class Output extends NodeConnector {
        public Output(NodeConnectorType type, UUID nodeId, String name) {
            super(type, nodeId, name);
        }
    }
}
