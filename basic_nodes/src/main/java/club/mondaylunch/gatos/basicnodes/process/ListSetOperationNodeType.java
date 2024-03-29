package club.mondaylunch.gatos.basicnodes.process;

import java.util.LinkedHashSet;
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

public class ListSetOperationNodeType extends NodeType.Process {
    private static final DataType<SetOperation> SET_OPERATION = DataType.register("set_operation", SetOperation.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "set_operation", SET_OPERATION.create(SetOperation.UNION)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "list_first", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "list_second", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputType = this.getOutputListTypeOrThrow(inputTypes, "list_first", "list_second");
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outputType));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputType = this.getOutputListTypeOrThrow(inputTypes, "list_first", "list_second");
        var currentOp = DataBox.get(settings, "set_operation", SET_OPERATION).orElseThrow();
        Set<Object> first = new LinkedHashSet<>(DataBox.get(inputs, "list_first", ListDataType.GENERIC_LIST).orElseThrow());
        Set<Object> second = new LinkedHashSet<>(DataBox.get(inputs, "list_second", ListDataType.GENERIC_LIST).orElseThrow());
        return Map.of("output",
            CompletableFuture.completedFuture(this.getGenericListBox(currentOp.compute(first, second), outputType))
        );
    }

    @SuppressWarnings("SameParameterValue")
    private DataType<?> getOutputListTypeOrThrow(Map<String, DataType<?>> inputTypes, String firstKey, String secondKey) {
        var first = inputTypes.getOrDefault(firstKey, ListDataType.GENERIC_LIST);
        if (!first.equals(inputTypes.getOrDefault(secondKey, ListDataType.GENERIC_LIST))) {
            throw new IllegalArgumentException("Both Lists are not of the same type.");
        }
        return first;
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    private enum SetOperation {
        UNION {
            @Override
            public <T> List<T> compute(Set<T> first, Set<T> second) {
                first.addAll(second);
                return first.stream().toList();
            }
        },
        INTERSECTION {
            @Override
            public <T> List<T> compute(Set<T> first, Set<T> second) {
                first.retainAll(second);
                return first.stream().toList();
            }
        },
        DIFFERENCE {
            @Override
            public <T> List<T> compute(Set<T> first, Set<T> second) {
                first.removeAll(second);
                return first.stream().toList();
            }
        };

        protected abstract <T> List<T> compute(Set<T> first, Set<T> second);
    }

    public static DataBox<SetOperation> getOperationSettingDataBox(String operation) {
        SetOperation op;
        try {
            op = SetOperation.valueOf(operation.toUpperCase());
        } catch (Exception e) {
            op = SetOperation.UNION;
        }
        return SET_OPERATION.create(op);
    }
}
