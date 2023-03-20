package club.mondaylunch.gatos.basicnodes;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class AddElementToListNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "list", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "element", DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        var listType = outputType == DataType.ANY ? ListDataType.GENERIC_LIST : outputType.listOf();
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", listType)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputList = DataBox.get(inputs, "list", ListDataType.GENERIC_LIST).orElse(List.of());
        var inputElem = DataBox.get(inputs, "element", DataType.ANY).orElseThrow();
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        
        if(outputType != this.findExactDatatype(inputs.get("list").type())) {
            return Map.of(
                "output", CompletableFuture.completedFuture(this.getGenericListBox(inputList, ListDataType.GENERIC_LIST))
            );
        }
        
        var listType = outputType == DataType.ANY ? ListDataType.GENERIC_LIST : outputType.listOf();
        
        return Map.of(
            "output", CompletableFuture.completedFuture(this.getGenericListBox(
                Stream.concat(inputList.stream(), Stream.of(inputElem)).collect(Collectors.toList()),
                listType))
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
