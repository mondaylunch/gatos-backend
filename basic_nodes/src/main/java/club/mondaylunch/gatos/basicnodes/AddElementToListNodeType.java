package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

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
            new NodeConnector.Input<>(nodeId, "list", this.getList(inputTypes)),
            new NodeConnector.Input<>(nodeId, "element", this.getElement(inputTypes))
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        DataType<?> outType = this.getList(inputTypes);
        
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outType)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputElem = DataBox.get(inputs, "element", DataType.ANY).orElseThrow();
        var inputList = DataBox.get(inputs, "list", ListDataType.GENERIC_LIST).orElseThrow();

        DataType<?> elemType = this.getElement(inputTypes);

        List<?> typedList = this.generateTypeList(elemType, inputList, inputElem);

        return Map.of(
            "output", CompletableFuture.completedFuture(this.getGenericListBox(typedList, this.getList(inputTypes)))
        );
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    private DataType<?>[] getTypesFromInputs(Map<String, DataType<?>> inputTypes) {
        DataType<?> elemType = inputTypes.getOrDefault("element", DataType.ANY);
        DataType<?> listRefType = inputTypes.getOrDefault("list", ListDataType.GENERIC_LIST);
        
        DataType<?> listType;
        if (elemType == DataType.ANY) {
            if (listRefType == ListDataType.GENERIC_LIST) {
                listType = ListDataType.GENERIC_LIST;
            }
            else {
                listType = listRefType;
                elemType = ((ListDataType<?>) listRefType).contains();
            }
        }
        else listType = elemType.listOf();

        DataType<?>[] out = new DataType<?>[4];
        out[0] = listType;
        out[1] = elemType;

        return out;
    }

    private DataType<?> getList(Map<String, DataType<?>> inputTypes) {
        return getTypesFromInputs(inputTypes)[0];
    }
    private DataType<?> getElement(Map<String, DataType<?>> inputTypes) {
        return getTypesFromInputs(inputTypes)[1];
    }

    private <T> List<T> generateTypeList(DataType<T> type, List<?> initialize, Object element) {
        //if (element) throw new IllegalArgumentException("The types of the given List and Element are incompatible.");

        List<T> output = new ArrayList<T>((List<T>) initialize);
        output.add((T) element);
        return output;
    }
}
