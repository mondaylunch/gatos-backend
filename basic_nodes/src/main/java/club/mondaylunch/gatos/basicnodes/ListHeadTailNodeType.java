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
import club.mondaylunch.gatos.core.data.OptionalDataType;
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
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        var optionalType = outputType == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : outputType.optionalOf();
        var listType = outputType == DataType.ANY ? ListDataType.GENERIC_LIST : outputType.listOf();
        return Set.of(
            new NodeConnector.Output<>(nodeId, "first", optionalType),
            new NodeConnector.Output<>(nodeId, "rest", listType)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(List.of());
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        var optionalType = outputType == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : outputType.optionalOf();
        var listType = outputType == DataType.ANY ? ListDataType.GENERIC_LIST : outputType.listOf();
        if (inputList.isEmpty()) {
            return Map.of(
                "first", CompletableFuture.completedFuture(this.getGenericOptionalBox(Optional.empty(), optionalType)),
                "rest", CompletableFuture.completedFuture(this.getGenericListBox(List.of(), listType))
            );
        }
        var shouldExtractHead = DataBox.get(settings, "extraction_mode",
            EXTRACTION_MODE_TYPE).orElse(ExtractionMode.EXTRACT_HEAD) == ExtractionMode.EXTRACT_HEAD;
        var lastIndex = inputList.size() - 1;
        var extractionIndex = shouldExtractHead ? 0 : lastIndex;
        var subListOffset = shouldExtractHead ? 1 : 0;
        var subList = inputList.subList(subListOffset, lastIndex + subListOffset);
        return Map.of(
            "first", CompletableFuture.completedFuture(
                this.getGenericOptionalBox(Optional.of(inputList.get(extractionIndex)), optionalType)
            ), "rest", CompletableFuture.completedFuture(
                this.getGenericListBox(subList, listType))
        );
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericOptionalBox(Optional<?> optional, DataType<?> type) {
        return ((DataType<T>) type.optionalOf()).create(((Optional<T>) optional).orElse((T) Optional.empty()));
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type.listOf()).create((T) list);
    }

    private DataType<?> findExactDatatype(DataType<?> type) {
        return type instanceof ListDataType<?> list
            ? list.contains()
            : DataType.ANY;
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
