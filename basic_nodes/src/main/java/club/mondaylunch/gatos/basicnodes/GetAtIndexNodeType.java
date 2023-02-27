package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class GetAtIndexNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "index", DataType.NUMBER)
        );
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("input", ListDataType.GENERIC_LIST);
        DataType<?> outType;

        if (inputType != ListDataType.GENERIC_LIST) {
            outType = inputType;
        } else {
            outType = DataType.ANY;
        }

        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outType));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(new ArrayList<>());
        Double inputIndex = DataBox.get(inputs, "index", DataType.NUMBER).orElse(-1.0);
        DataType outType = inputTypes.getOrDefault("input", DataType.ANY);
        int index = inputIndex.intValue();
        boolean ListOutOfBound = index == inputList.size()-1;

        if (ListOutOfBound || inputList.isEmpty()) {
            return Map.of("output", CompletableFuture.completedFuture(outType.create(OptionalDataType.GENERIC_OPTIONAL)));
        } else {
            return Map.of("output", CompletableFuture.completedFuture(outType.create(inputList.get(index))));
        }
    }
    
}
