package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class VariableRemappingNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.JSON_OBJECT),
            new NodeConnector.Input<>(nodeId, "oldKey", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "newKey", DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.JSON_OBJECT)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var jsonInput = DataBox.get(inputs, "input", DataType.JSON_OBJECT).orElse(new JsonObject());
        var oldKeyStr = DataBox.get(inputs, "oldKey", DataType.STRING).orElse("");
        var newKeyStr = DataBox.get(inputs, "newKey", DataType.STRING).orElse("");
        var value = jsonInput.get(oldKeyStr);
        if (oldKeyStr.equals(newKeyStr) || value == null) {
            return Map.of("output", CompletableFuture.completedFuture(DataType.JSON_OBJECT.create(jsonInput)));
        }
        jsonInput.add(newKeyStr, jsonInput.remove(oldKeyStr));
        return Map.of(
            "output", CompletableFuture.completedFuture(DataType.JSON_OBJECT.create(jsonInput))
        );
    }
}
