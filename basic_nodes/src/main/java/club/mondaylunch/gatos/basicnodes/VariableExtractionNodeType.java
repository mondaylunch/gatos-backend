package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonNull;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class VariableExtractionNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
                new NodeConnector.Input<>(nodeId, "input", DataType.JSON_OBJECT),
                new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
                new NodeConnector.Output<>(nodeId, "output", DataType.JSON_ELEMENT));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
            Map<String, DataBox<?>> settings) {
        var jsonInput = DataBox.get(inputs, "input", DataType.JSON_OBJECT).orElse(null);
        var keyStr = DataBox.get(inputs, "key", DataType.STRING).orElse(null);
        if (jsonInput == null || keyStr == null || jsonInput.get(keyStr) == null) {
            return Map.of("output",
                    CompletableFuture.completedFuture(DataType.JSON_ELEMENT.create(JsonNull.INSTANCE)));
        }
        var value = jsonInput.get(keyStr);
        return Map.of("output",
                CompletableFuture.completedFuture(DataType.JSON_ELEMENT.create(value)));
    }
}
