package club.mondaylunch.gatos.basicnodes.end;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class WebhookEndNodeType extends NodeType.End {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "graphOutput", DataType.JSON_OBJECT),
            new NodeConnector.Input<>(nodeId, "outputReference", DataType.REFERENCE)
        );
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var graphOutput = DataBox.get(
            inputs,
            "graphOutput",
            DataType.JSON_OBJECT
        ).orElseThrow();
        var outputReference = DataBox.get(
            inputs,
            "outputReference",
            DataType.REFERENCE
        ).orElseThrow();
        outputReference.setValue(graphOutput);
        return CompletableFuture.completedFuture(null);
    }
}
