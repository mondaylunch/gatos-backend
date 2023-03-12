package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
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
        DataType<?> outType = inputType != ListDataType.GENERIC_LIST ? inputType : DataType.ANY;
        System.out.println(inputType.name());

        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outType));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(new ArrayList<>());
        Double inputIndex = DataBox.get(inputs, "index", DataType.NUMBER).orElse(-1.0);
        int index = inputIndex.intValue();
        boolean ListOutOfBound = index == inputList.size()-1;

        var inputType = inputTypes.getOrDefault("input", ListDataType.GENERIC_LIST);
        DataType<?> outType = inputType != ListDataType.GENERIC_LIST ? inputType : DataType.ANY;
        System.out.println(outType);

        // var outputType = this.findExactDatatype(inputTypes.get("input"));
        // var optionalType = outputType == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : outputType.optionalOf();
        // var listType = outputType == DataType.ANY ? DataType.ANY : outputType;
        if (ListOutOfBound || inputList.isEmpty()) {
            return Map.of("output", CompletableFuture.completedFuture(this.getGenericOptionalBox(Optional.empty(), outType)));
        } else {
            return Map.of("output", CompletableFuture.completedFuture(this.getGenericListBox(inputList.get(index), outType)));
        }
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericOptionalBox(Optional<?> optional, DataType<?> type) {
        return ((DataType<T>) type).create(((Optional<T>) optional).orElse((T) Optional.empty()));
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(Object ob, DataType<?> type) {
        return ((DataType<T>) type).create((T) ob);
    }
}
