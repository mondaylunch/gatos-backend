package club.mondaylunch.gatos.basicnodes.process;

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

public abstract class AbstractListSeparationNodeType extends NodeType.Process {
    protected boolean shouldExtractHeadElem;

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
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
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
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
        var lastIndex = inputList.size() - 1;
        var extractionIndex = this.shouldExtractHeadElem ? 0 : lastIndex;
        var subListOffset = this.shouldExtractHeadElem ? 1 : 0;
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
        return ((DataType<T>) type).create(((Optional<T>) optional).orElse((T) Optional.empty()));
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    private DataType<?> findExactDatatype(DataType<?> type) {
        return type instanceof ListDataType<?> list
            ? list.contains()
            : DataType.ANY;
    }
}
