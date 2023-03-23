package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class EmptyOptionalNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "containing_type", DataType.DATA_TYPE.create(DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of();
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var containingType = DataBox.get(settings, "containing_type", DataType.DATA_TYPE).orElseThrow();
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", containingType.optionalOf())
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var containingType = DataBox.get(settings, "containing_type", DataType.DATA_TYPE).orElseThrow();
        return Map.of("output", CompletableFuture.completedFuture(containingType.optionalOf().create(Optional.empty())));
    }
}
