package club.mondaylunch.gatos.core.graph.connector;

import java.util.Objects;
import java.util.UUID;

import club.mondaylunch.gatos.core.data.DataType;

/**
 * Represents an input or output on a node.
 * @param <T> the type of data this node takes in or spits out
 */
public abstract sealed class NodeConnector<T> {
    private final UUID nodeId;
    private final String name;
    private final DataType<T> dataType;

    protected NodeConnector(UUID nodeId, String name, DataType<T> dataType) {
        this.nodeId = nodeId;
        this.name = name;
        this.dataType = dataType;
    }

    /**
     * Returns the UUID of the node this connector belongs to.
     * @return the UUID of the node this belongs to
     */
    public UUID nodeId() {
        return this.nodeId;
    }

    /**
     * Returns the name of this connector.
     * @return the name of this connector
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the datatype of this connector.
     * @return the datatype of this connector
     */
    public DataType<T> type() {
        return this.dataType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        var that = (NodeConnector<?>) obj;
        return Objects.equals(this.nodeId, that.nodeId)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.name, this.dataType);
    }

    /**
     * An input connector on a node.
     * @param <T> the type of data this node takes in
     */
    public static final class Input<T> extends NodeConnector<T> {
        public Input(UUID nodeId, String name, DataType<T> dataType) {
            super(nodeId, name, dataType);
        }
    }

    /**
     * An output connector on a node.
     * @param <T> the type of data this node spits out
     */
    public static final class Output<T> extends NodeConnector<T> {
        public Output(UUID nodeId, String name, DataType<T> dataType) {
            super(nodeId, name, dataType);
        }
    }
}
