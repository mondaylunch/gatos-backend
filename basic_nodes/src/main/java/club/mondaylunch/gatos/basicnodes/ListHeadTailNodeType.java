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

public class ListHeadTailNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "head_mode", DataType.BOOLEAN.create(true)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output_first", DataType.ANY),
            new NodeConnector.Output<>(nodeId, "output_rest", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(List.of());
        if (inputList.isEmpty()) {
            return Map.of(
                "output_first", CompletableFuture.completedFuture(DataType.ANY.create(null)),
                "output_rest", CompletableFuture.completedFuture(ListDataType.ANY.create(null))
            );
        }
        var shouldExtractHead = DataBox.get(settings, "head_mode", DataType.BOOLEAN).orElse(true);
        var extractionIndex = shouldExtractHead ? 0 : inputList.size() - 1;
        return Map.of(
            "output_first", CompletableFuture.completedFuture(DataType.ANY.create(inputList.get(extractionIndex))),
            "output_rest", CompletableFuture.completedFuture(ListDataType.ANY.create(inputList.subList(
                shouldExtractHead ? 1 : 0,
                shouldExtractHead ? inputList.size() : inputList.size() - 1)))
        );
        // indexes 0-9
        // head: 0, [1-9]
        // tail: 9, [0-8]
    }
}
