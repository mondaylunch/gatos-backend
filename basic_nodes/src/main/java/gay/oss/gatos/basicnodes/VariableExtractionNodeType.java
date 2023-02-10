package gay.oss.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.type.NodeType;

public class VariableExtractionNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "output_type", DataType.DATATYPE.create(DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.JSONOBJECT),
            new NodeConnector.Input<>(nodeId, "key", DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", this.settings().get("output_type").type().optionalOf())
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var jsonInput = DataBox.get(inputs, "input", DataType.JSONOBJECT).orElse(new JsonObject());
        var keyStr = DataBox.get(inputs, "key", DataType.STRING).orElse("");
        var returnType = DataBox.get(settings, "output_type", DataType.DATATYPE).orElse(DataType.STRING);
        var value = jsonInput.get(keyStr);
        if (value == null) {
            return Map.of("output", CompletableFuture.completedFuture(
                returnType.optionalOf().create(Optional.empty()))
            );
        }
        Map<String, CompletableFuture<DataBox<?>>> returnMap;
        switch (ReturnType.getFromDataType(returnType)) {
            case INTEGER -> returnMap = Map.of("output", CompletableFuture.completedFuture(
                DataType.INTEGER.optionalOf().create(Optional.of(value.getAsInt()))));
            case BOOLEAN -> returnMap = Map.of("output", CompletableFuture.completedFuture(
                DataType.BOOLEAN.optionalOf().create(Optional.of(value.getAsBoolean()))));
            case STRING -> returnMap = Map.of("output", CompletableFuture.completedFuture(
                DataType.STRING.optionalOf().create(Optional.of(value.getAsString()))));
            case JSONOBJECT -> returnMap = Map.of("output", CompletableFuture.completedFuture(
                DataType.JSONOBJECT.optionalOf().create(Optional.of(value.getAsJsonObject()))));
            case LIST -> returnMap = Map.of("output", this.handleListReturn(returnType, value));
            default -> returnMap = Map.of("output", CompletableFuture.completedFuture(
                DataType.STRING.optionalOf().create(Optional.of(new Gson().toJson(value.toString())))));
        }
        return returnMap;
    }

    private CompletableFuture handleListReturn(DataType dataType, JsonElement jsonElement) {
        List<Object> outputList = new ArrayList<>();
        if (jsonElement instanceof JsonArray arr && arr.size() > 0 && arr.get(0) instanceof JsonPrimitive primitive) {
            if (primitive.isNumber()) {
                arr.forEach(prim -> outputList.add(prim.getAsInt()));
            } else if (primitive.isBoolean()) {
                arr.forEach(prim -> outputList.add(prim.getAsBoolean()));
            } else if (primitive.isString()) {
                arr.forEach(prim -> outputList.add(prim.getAsString()));
            } else if (primitive.isJsonObject()) {
                arr.forEach(prim -> outputList.add(prim.getAsJsonObject()));
            }
        }
        return CompletableFuture.completedFuture(dataType.optionalOf().create(Optional.of(outputList)));
    }

    private enum ReturnType {
        INTEGER(DataType.INTEGER),
        BOOLEAN(DataType.BOOLEAN),
        STRING(DataType.STRING),
        JSONOBJECT(DataType.JSONOBJECT),
        LIST(DataType.INTEGER.listOf(),
            DataType.STRING.listOf(),
            DataType.BOOLEAN.listOf(),
            DataType.JSONOBJECT.listOf()
        );
        private final DataType[] dataType;
        ReturnType(DataType... dataType) {
            this.dataType = dataType;
        }

        public static ReturnType getFromDataType(DataType dataType) {
            var type = Arrays.stream(ReturnType.values())
                .filter(x -> Arrays.stream(x.dataType).toList().contains(dataType)).toList();
            return type.size() > 0 ? type.get(0) : STRING;
        }
    }
}
