package club.mondaylunch.gatos.basicnodes;

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
        DataType<?> listType = inputTypes.getOrDefault("list", ListDataType.ANY);
        DataType<?> elemType = inputTypes.getOrDefault("element", DataType.ANY);
        
        return Set.of(
            new NodeConnector.Input<>(nodeId, "list", listType),
            new NodeConnector.Input<>(nodeId, "element", elemType)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", inputTypes.get("list"))
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputElem = DataBox.get(inputs, "element", DataType.ANY).orElseThrow();
        var inputList = DataBox.get(inputs, "list", ListDataType.GENERIC_LIST).orElseThrow();


        List<Object> copyList = new ArrayList<>(inputList);
        copyList.add(inputElem);
        

        return Map.of(
            "output", CompletableFuture.completedFuture(this.getGenericListBox(copyList, inputTypes.get("list")))
        );
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }
}
