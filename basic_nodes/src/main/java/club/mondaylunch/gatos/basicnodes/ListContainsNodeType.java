package club.mondaylunch.gatos.basicnodes;

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

public class ListContainsNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "list", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "element", DataType.ANY));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputListType = inputTypes.get("list");
        var elementType = inputTypes.get("element");
        if (inputListType != elementType.listOf()) {
            throw new IllegalArgumentException("The Element's type does not match that of the List.");
        }
        var inputList = (List<?>) DataBox.get(inputs, "list", ListDataType.GENERIC_LIST).orElseThrow();
        var element = DataBox.get(inputs, "element", DataType.ANY).orElseThrow();
        return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(inputList.contains(element))));
    }
}
