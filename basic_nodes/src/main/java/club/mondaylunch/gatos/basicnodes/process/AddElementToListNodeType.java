package club.mondaylunch.gatos.basicnodes.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class AddElementToListNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputListType = this.getExactListType(inputTypes.getOrDefault("list", ListDataType.GENERIC_LIST));
        var inputElemType = this.getExactElemType(inputListType);

        return Set.of(
            new NodeConnector.Input<>(nodeId, "list", inputListType),
            new NodeConnector.Input<>(nodeId, "element", inputElemType)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var output = this.getExactListType(inputTypes.getOrDefault("list", ListDataType.GENERIC_LIST));
        
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", output)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputListType = this.getExactListType(inputTypes.getOrDefault("list", ListDataType.GENERIC_LIST));
        var inputElemType = this.getExactElemType(inputListType);
        var inputList = (List<?>) DataBox.get(inputs, "list", inputListType).orElseThrow();
        var inputElem = DataBox.get(inputs, "element", inputElemType).orElseThrow();

        var copyList = new ArrayList<>(inputList);
        var output = this.generateList(copyList, inputElem);
        return Map.of(
            "output", CompletableFuture.completedFuture(this.getGenericListBox(output, inputListType))
        );
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    private DataType<?> getExactListType(DataType<?> type) {
        return type instanceof ListDataType<?> list ? list : ListDataType.GENERIC_LIST;
    }

    private DataType<?> getExactElemType(DataType<?> type) {
        return type instanceof ListDataType<?> list ? list.contains() : DataType.ANY;
    }

    @SuppressWarnings("unchecked")
    private <T> List<?> generateList(ArrayList<T> list, Object element) {
        list.add((T) element);
        return list; 
    }
}
