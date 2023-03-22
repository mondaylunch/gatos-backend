package club.mondaylunch.gatos.basicnodes.end;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public abstract class UpdateFlowDataNodeType extends NodeType.End {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create(""),
            "set_if_absent", DataType.BOOLEAN.create(true)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        Set<NodeConnector.Input<?>> inputs = new HashSet<>();
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            inputs.add(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        }
        inputs.add(new NodeConnector.Input<>(nodeId, "value", DataType.NUMBER));
        return Collections.unmodifiableSet(inputs);
    }

    @Override
    public CompletableFuture<Void> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING).orElseThrow();
        var value = DataBox.get(inputs, "value", DataType.NUMBER).orElseThrow();
        boolean setIfAbsent = DataBox.get(settings, "set_if_absent", DataType.BOOLEAN).orElse(true);
        return CompletableFuture.runAsync(() -> this.updateValue(flowId, key, value, setIfAbsent));
    }

    protected abstract void updateValue(UUID flowId, String key, double value, boolean setIfAbsent);
}
