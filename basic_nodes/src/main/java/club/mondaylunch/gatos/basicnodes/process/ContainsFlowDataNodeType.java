package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.FlowData;

public class ContainsFlowDataNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create(""),
            "type", DataType.DATA_TYPE.create(DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            return Set.of(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        } else {
            return Set.of();
        }
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(new NodeConnector.Output<>(nodeId, "contains", DataType.BOOLEAN));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING).orElseThrow();
        var type = DataBox.get(settings, "type", DataType.DATA_TYPE).orElse(DataType.ANY);
        return Map.of(
            "contains", CompletableFuture.supplyAsync(() -> {
                boolean contains;
                if (type.equals(DataType.ANY)) {
                    contains = FlowData.objects.contains(flowId, key);
                } else {
                    contains = FlowData.objects.contains(flowId, key, type);
                }
                return DataType.BOOLEAN.create(contains);
            })
        );
    }
}
