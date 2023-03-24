package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ParseStringToObjectNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.JSON_OBJECT));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputStr = DataBox.get(inputs, "input", DataType.STRING).orElseThrow();
        JsonElement js;
        try {
            js = JsonParser.parseString(inputStr);
        } catch (Exception l) {
            js = new JsonPrimitive(inputStr);
        }
        return Map.of("output", CompletableFuture.completedFuture(DataType.JSON_OBJECT.create(
            js.isJsonObject() ? js.getAsJsonObject() : this.createObject(js))
            )
        );
    }

    private JsonObject createObject(JsonElement value) {
        var json = new JsonObject();
        json.add("value", value);
        return json;
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs) {
        return this.compute(UUID.randomUUID(), inputs, Map.of(), Map.of());
    }
}
