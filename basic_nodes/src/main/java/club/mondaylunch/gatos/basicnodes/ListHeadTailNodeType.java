package club.mondaylunch.gatos.basicnodes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ListHeadTailNodeType extends NodeType.Process {
    private static final DataType<ExtractionMode> EXTRACTION_MODE_TYPE = DataType.register("extraction_mode_type", ExtractionMode.class);
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "extraction_mode", getHeadExtractionBox()
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var z = inputTypes;
        return Set.of(
            new NodeConnector.Output<>(nodeId, "first", DataType.ANY),
            new NodeConnector.Output<>(nodeId, "rest", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(List.of());
        if (inputList.isEmpty()) {
            return Map.of(
                "first", CompletableFuture.completedFuture(DataType.ANY.create(Optional.empty())),
                "rest", CompletableFuture.completedFuture(ListDataType.GENERIC_LIST.create(List.of()))
            );
        }
        var shouldExtractHead = DataBox.get(settings, "extraction_mode",
            EXTRACTION_MODE_TYPE).orElse(ExtractionMode.EXTRACT_HEAD) == ExtractionMode.EXTRACT_HEAD;
        var lastIndex = inputList.size() - 1;
        var extractionIndex = shouldExtractHead ? 0 : lastIndex;
        var subListOffset = shouldExtractHead ? 1 : 0;
        var outputType = this.getTypeFromClass(inputList.get(0).getClass());
        return Map.of(
            "first", CompletableFuture.completedFuture(outputType.create(
                inputList.get(extractionIndex))),
            "rest", CompletableFuture.completedFuture(outputType.listOf().create(
                (List<Object>) inputList.subList(subListOffset, lastIndex + subListOffset)))
        );
    }

    @SuppressWarnings("unchecked")
    private <T> DataType<T> getTypeFromClass(Class<?> klass) {
        DataType type;
        if (klass == Double.class) {
            type = DataType.NUMBER;
        } else if (klass == String.class) {
            type = DataType.STRING;
        } else if (klass == Boolean.class) {
            type = DataType.BOOLEAN;
        } else if (klass == JsonObject.class) {
            type = DataType.JSON_OBJECT;
        } else {
            type = DataType.ANY;
        }
        return type;
    }

    private enum ExtractionMode {
        EXTRACT_HEAD,
        EXTRACT_TAIL
    }

    public static DataBox<ExtractionMode> getHeadExtractionBox() {
        return EXTRACTION_MODE_TYPE.create(ExtractionMode.EXTRACT_HEAD);
    }

    public static DataBox<ExtractionMode> getTailExtractionBox() {
        return EXTRACTION_MODE_TYPE.create(ExtractionMode.EXTRACT_TAIL);
    }
}
