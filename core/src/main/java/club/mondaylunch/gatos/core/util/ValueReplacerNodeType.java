package club.mondaylunch.gatos.core.util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * A node type that provides a value, discarding any input.
 */
public class ValueReplacerNodeType<T> extends NodeType.Process {
    private final DataType<T> type;
    private final T defaultValue;

    public ValueReplacerNodeType(DataBox<T> box) {
        this.type = box.type();
        this.defaultValue = box.value();
    }

    public ValueReplacerNodeType(DataType<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "value", this.type.create(this.defaultValue)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "dummy", DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", this.type)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Map.of(
            "output", CompletableFuture.completedFuture(this.type.create(DataBox.get(settings, "value", this.type).orElse(this.defaultValue)))
        );
    }
}
