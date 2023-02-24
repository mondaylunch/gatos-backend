package club.mondaylunch.gatos.basicnodes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
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
            new NodeConnector.Input<>(nodeId, "input", ListDataType.GENERIC_LIST),
            new NodeConnector.Input<>(nodeId, "in", OptionalDataType.GENERIC_OPTIONAL)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputType = inputTypes.getOrDefault("in", OptionalDataType.GENERIC_OPTIONAL);
        if (outputType == OptionalDataType.GENERIC_OPTIONAL) {
            outputType = DataType.ANY;
        } else {
            outputType = ((OptionalDataType<?>) outputType).contains();
        }
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output_first", outputType),
            new NodeConnector.Output<>(nodeId, "output_rest", ListDataType.GENERIC_LIST)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var inputList = DataBox.get(inputs, "input", ListDataType.GENERIC_LIST).orElse(List.of());
        if (inputList.isEmpty()) {
            return Map.of(
                "output_first", CompletableFuture.completedFuture(DataType.ANY.create(null)),
                "output_rest", CompletableFuture.completedFuture(ListDataType.GENERIC_LIST.create(null))
            );
        }
        var shouldExtractHead = DataBox.get(settings, "head_mode", DataType.BOOLEAN).orElse(true);
        var lastIndex = inputList.size() - 1;
        var extractionIndex = shouldExtractHead ? 0 : lastIndex;
        var subListOffset = shouldExtractHead ? 1 : 0;
        return Map.of(
            "output_first", CompletableFuture.completedFuture(DataType.ANY.create(
                inputList.get(extractionIndex))),
            "output_rest", CompletableFuture.completedFuture(ListDataType.GENERIC_LIST.create(
                inputList.subList(subListOffset, lastIndex + subListOffset)))
        );
    }
}