package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ListSortNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputType = inputTypes.get("input");
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outputType));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputListCopy = new ArrayList<>(DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(List.of()));
        var outputType = inputTypes.get("input");
        if (!inputListCopy.isEmpty() && this.containsOnlyComparables(inputListCopy)) {
            Collections.sort((List<Comparable>) inputListCopy);
        }
        return Map.of("output", CompletableFuture.completedFuture(
            this.getGenericListBox(inputListCopy.stream().toList(), outputType))
        );
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    private boolean containsOnlyComparables(List<?> list) {
        return list.stream().filter(item -> !(item instanceof Comparable)).toList().isEmpty();
    }
}
