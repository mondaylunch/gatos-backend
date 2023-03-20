package club.mondaylunch.gatos.basicnodes;

import static java.util.stream.Collectors.toList;

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

public class ListSetOperationNodeType extends NodeType.Process {
    public static final DataType<SetOperation> SET_OPERATION = DataType.register("set_operation", SetOperation.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
                "mode", SET_OPERATION.create(SetOperation.UNION)
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
        var outputType = inputTypes.getOrDefault("list_type", ListDataType.GENERIC_LIST);
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", outputType));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var currentOp = DataBox.get(settings, "set_operation", SET_OPERATION).orElseThrow();
        var first = new ArrayList<>(DataBox.get(inputs, "list_first", ListDataType.GENERIC_LIST).orElseThrow());
        var second = new ArrayList<>(DataBox.get(inputs, "list_second", ListDataType.GENERIC_LIST).orElseThrow());
        return Map.of();
    }

    @SuppressWarnings({"unchecked", "ListUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericListBox(List<?> list, DataType<?> type) {
        return ((DataType<T>) type).create((T) list);
    }

    public enum SetOperation {
        UNION {
            @Override
            public <T> List<T> apply(List<T> first, List<T> second) {
                first.addAll(second);
                return first.stream().distinct().collect(toList());
            }
        },
        INTERSECTION {
            @Override
            public <T> List<T> apply(List<T> first, List<T> second) {
                return first.stream().distinct().filter(second::contains).collect(toList());
            }
        },
        DIFFERENCE {
            @Override
            public <T> List<T> apply(List<T> first, List<T> second) {
                return first.stream().distinct().filter(o -> !second.contains(o)).collect(toList());
            }
        };
        public abstract <T> List<T> apply(List<T> first, List<T> second);
    }
}
