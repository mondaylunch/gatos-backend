package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ValueProviderNodeType<T> extends NodeType.Process {
    private final DataType<T> type;
    private final T defaultValue;

    public ValueProviderNodeType(DataBox<T> box) {
        this.type = box.type();
        this.defaultValue = box.value();
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "value", this.type.create(this.defaultValue)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of();
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", this.type)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Map.of(
            "output", CompletableFuture.completedFuture(this.type.create(DataBox.get(settings, "value", this.type).orElse(this.defaultValue)))
        );
    }
}
