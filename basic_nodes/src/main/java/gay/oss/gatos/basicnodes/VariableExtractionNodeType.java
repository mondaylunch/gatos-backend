package gay.oss.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.type.NodeType;

public class VariableExtractionNodeType extends NodeType.Process {
    private static final DataType<ReturnType> RETURN_DATATYPE = new DataType<>("return_type");
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "output_type", RETURN_DATATYPE.create(ReturnType.STRING)
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
        var returnDataType = !state.isEmpty()
            ? ((ReturnType) state.get("output_type").value())
            : ReturnType.STRING;
        return returnDataType.isListType()
            ? Set.of(new NodeConnector.Output<>(nodeId, "output", returnDataType.getDataType()))
            : Set.of(new NodeConnector.Output<>(nodeId, "output", returnDataType.getDataType().optionalOf())
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var jsonInput = DataBox.get(inputs, "input", DataType.JSONOBJECT).orElse(new JsonObject());
        var keyStr = DataBox.get(inputs, "key", DataType.STRING).orElse("");
        var returnType = !settings.isEmpty() ? (ReturnType) settings.get("output_type").value() : ReturnType.STRING;
        var value = jsonInput.get(keyStr);
        if (value == null) {
            return Map.of("output", CompletableFuture.completedFuture(
                returnType.getDataType().optionalOf().create(Optional.empty()))
            );
        }
        return switch (returnType) {
            case INTEGER -> Map.of("output", CompletableFuture.completedFuture(
                DataType.INTEGER.optionalOf().create(Optional.of(value.getAsInt()))));
            case BOOLEAN -> Map.of("output", CompletableFuture.completedFuture(
                DataType.BOOLEAN.optionalOf().create(Optional.of(value.getAsBoolean()))));
            case STRING -> Map.of("output", CompletableFuture.completedFuture(
                DataType.STRING.optionalOf().create(Optional.of(value.getAsString()))));
            case JSONOBJECT -> Map.of("output", CompletableFuture.completedFuture(
                DataType.JSONOBJECT.optionalOf().create(Optional.of(value.getAsJsonObject()))));
            case INTEGER_LIST,
                STRING_LIST,
                BOOLEAN_LIST,
                JSONOBJECT_LIST -> Map.of("output", this.handleListReturn(returnType.getDataType(), value));
        };
    }

    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<DataBox<?>> handleListReturn(DataType<T> dataType, JsonElement jsonElement) {
        List<Object> outputList = new ArrayList<>();
        if (jsonElement instanceof JsonArray arr && arr.size() > 0) {
            arr.forEach(json -> outputList.add(this.jsonToReturnableType(json)));
        } else {
            outputList.add(this.jsonToReturnableType(jsonElement));
        }
        return CompletableFuture.completedFuture(dataType.create((T) outputList.stream().filter(Objects::nonNull).toList()));
    }

    @Nullable
    private Object jsonToReturnableType(JsonElement jsonElement) {
        if (jsonElement instanceof JsonPrimitive prim) {
            if (prim.isNumber()) {
                return jsonElement.getAsInt();
            } else if (prim.isBoolean()) {
                return jsonElement.getAsBoolean();
            } else if (prim.isString()) {
                return jsonElement.getAsString();
            }
        }
        return jsonElement;
    }

    private enum ReturnType {
        INTEGER(DataType.INTEGER),
        BOOLEAN(DataType.BOOLEAN),
        STRING(DataType.STRING),
        JSONOBJECT(DataType.JSONOBJECT),
        INTEGER_LIST(DataType.INTEGER.listOf()),
        STRING_LIST(DataType.STRING.listOf()),
        BOOLEAN_LIST(DataType.BOOLEAN.listOf()),
        JSONOBJECT_LIST(DataType.JSONOBJECT.listOf());
        private final DataType<?> dataType;
        ReturnType(DataType<?> dataType) {
            this.dataType = dataType;
        }

        public DataType<?> getDataType() {
            return this.dataType;
        }

        public boolean isListType() {
            return List.of(INTEGER_LIST, STRING_LIST, BOOLEAN_LIST, JSONOBJECT_LIST).contains(this);
        }
    }

    public static ReturnType getFromDataType(DataType<?> dataType) {
        var type = Arrays.stream(ReturnType.values())
            .filter(x -> x.dataType == dataType).toList();
        return type.size() > 0 ? type.get(0) : ReturnType.STRING;
    }

    public static DataBox<ReturnType> getReturnBoxFromType(DataType<?> dataType) {
        return RETURN_DATATYPE.create(getFromDataType(dataType));
    }
}
