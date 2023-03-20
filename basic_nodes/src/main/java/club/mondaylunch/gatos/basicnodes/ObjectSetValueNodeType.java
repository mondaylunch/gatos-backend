package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ObjectSetValueNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create("")
        );
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "object", DataType.JSON_OBJECT),
            new NodeConnector.Input<>(nodeId, "element", DataType.JSON_ELEMENT)
        );
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.JSON_OBJECT)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = (String) settings.get("key").value();
        var jsonObject = DataBox.get(inputs, "object", DataType.JSON_OBJECT).orElse(new JsonObject());
        var jsonElement = DataBox.get(inputs, "element", DataType.JSON_ELEMENT);

        // the add method will update a value if it exists or add a new value if a key doesn't exist
        // constructor is deprecated for json element so we will check if the json element is empty or not
        if (!("".equals(key)) && jsonElement.isPresent()) {
            jsonObject.add(key, jsonElement.get());
        }

        return Map.of("output", CompletableFuture.completedFuture(DataType.JSON_OBJECT.create(jsonObject)));
    }
    
}
