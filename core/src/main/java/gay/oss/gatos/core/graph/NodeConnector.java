package gay.oss.gatos.core.graph;

import java.util.Objects;
import java.util.UUID;

public sealed abstract class NodeConnector<T extends NodeConnectorType<?>> {
    private final UUID nodeId;
    private final String name;

    protected NodeConnector(UUID nodeId, String name) {
        this.nodeId = nodeId;
        this.name = name;
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
        var that = (NodeConnector<?>) obj;
        return  Objects.equals(this.nodeId, that.nodeId) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.name);
    }

    public static final class Input<T extends NodeConnectorType<?>> extends NodeConnector<T> {
        public Input(UUID nodeId, String name) {
            super(nodeId, name);
        }
    }

    public static final class Output<T extends NodeConnectorType<?>> extends NodeConnector<T> {
        public Output(UUID nodeId, String name) {
            super(nodeId, name);
        }
    }
}
