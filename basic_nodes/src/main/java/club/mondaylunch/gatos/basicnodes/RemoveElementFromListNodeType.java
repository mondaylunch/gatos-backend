package club.mondaylunch.gatos.basicnodes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class RemoveElementFromListNodeType extends NodeType.Process {

    public static final DataType<Mode> ELEMENT_REFERENCE = DataType.register("methodofreferencingelement", Mode.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "mode", ELEMENT_REFERENCE.create(Mode.ELEMENT)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var mode = DataBox.get(settings, "mode", ELEMENT_REFERENCE).orElse(Mode.ELEMENT);
        if (mode == Mode.ELEMENT) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "list", ListDataType.GENERIC_LIST),
                new NodeConnector.Input<>(nodeId, "element", DataType.ANY)
            );
        }
        
        return Set.of(
            new NodeConnector.Input<>(nodeId, "list", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "index", DataType.NUMBER)
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
        var mode = DataBox.get(settings, "mode", ELEMENT_REFERENCE).orElse(Mode.ELEMENT);
        var inputList = DataBox.get(inputs, "list", ListDataType.GENERIC_LIST).orElse(List.of());
        var outputType = this.findExactDatatype(inputTypes.get("input"));
        var listType = outputType == DataType.ANY ? ListDataType.GENERIC_LIST : outputType.listOf();
        int index = -1;

        if (mode == Mode.ELEMENT) {
            var inputElem = DataBox.get(inputs, "element", DataType.ANY).orElseThrow();
            try {
                index = inputList.indexOf(inputElem);
            } catch (ClassCastException e) {
                return Map.of(
                    "output", CompletableFuture.completedFuture(this.getGenericListBox(inputList, listType))
                );
            }
        }

        if (mode == Mode.INDEX) index = (int) (double) DataBox.get(inputs, "index", DataType.NUMBER).orElseThrow();

        System.out.println(index);

        if (index == -1) return Map.of("output", CompletableFuture.completedFuture(this.getGenericListBox(inputList, listType)));
   
        return Map.of(
            "output", CompletableFuture.completedFuture(this.getGenericListBox(
                Stream.concat(
                    inputList.subList(0, index).stream(),
                    inputList.subList(index + 1, inputList.size()).stream())
                    .collect(Collectors.toList()),
                listType))
        );
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

    public enum Mode {
        INDEX,
        ELEMENT;
    }
}
