package club.mondaylunch.gatos.basicnodes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outType = inputTypes.isEmpty() ? DataType.ANY : inputTypes.get("input");
        return Set.of(
            new NodeConnector.Output<>(nodeId, "first", outType),
            new NodeConnector.Output<>(nodeId, "rest", outType.listOf())
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
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
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        return Map.of(
            "first", CompletableFuture.completedFuture(outputType.create(
                inputList.get(extractionIndex))),
            "rest", CompletableFuture.completedFuture(outputType.listOf().create(
                (List<Object>) inputList.subList(subListOffset, lastIndex + subListOffset)))
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> DataType<T> findExactDatatype(DataType type) {
        if (type.equals(DataType.NUMBER)) {
            type = DataType.NUMBER;
        } else if (type.equals(DataType.STRING)) {
            type = DataType.STRING;
        } else if (type.equals(DataType.BOOLEAN)) {
            type = DataType.BOOLEAN;
        } else if (type.equals(DataType.JSON_OBJECT)) {
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
