package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public String name() {
        return "variable_remapping";
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
                new NodeConnector.Input<>(nodeId, "input", DataType.JSONOBJECT),
                new NodeConnector.Input<>(nodeId, "oldKey", DataType.STRING),
                new NodeConnector.Input<>(nodeId, "newKey", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
                new NodeConnector.Output<>(nodeId, "output", DataType.JSONOBJECT));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
            Map<String, DataBox<?>> settings) {
        var jsonInput = DataBox.get(inputs, "input", DataType.JSONOBJECT).orElse(null);
        var oldKeyStr = DataBox.get(inputs, "oldKey", DataType.STRING).orElse(null);
        var newKeyStr = DataBox.get(inputs, "newKey", DataType.STRING).orElse(null);
        if (jsonInput == null || oldKeyStr == null || newKeyStr == null || oldKeyStr.equals(newKeyStr)
                || jsonInput.get(oldKeyStr) == null) {
            return Map.of("output", CompletableFuture.completedFuture(DataType.JSONOBJECT.create(jsonInput)));
        }
        var value = jsonInput.get(oldKeyStr);
        jsonInput.add(newKeyStr, jsonInput.remove(oldKeyStr));
        return Map.of(
                "output", CompletableFuture.completedFuture(DataType.JSONOBJECT.create(jsonInput)));
    }
}
