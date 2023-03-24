package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class OptionalFilterNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "optional", OptionalDataType.GENERIC_OPTIONAL),
            new NodeConnector.Input<>(nodeId, "condition", DataType.BOOLEAN)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("optional", OptionalDataType.GENERIC_OPTIONAL);
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", inputType)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("optional", OptionalDataType.GENERIC_OPTIONAL);
        Optional<?> input = (Optional<?>) DataBox.get(inputs, "optional", inputType).orElseThrow();
        var condition = DataBox.get(inputs, "condition", DataType.BOOLEAN).orElseThrow();
        return Map.of("output", CompletableFuture.completedFuture(
            this.getGenericOptionalBox(input.filter($ -> condition), ((OptionalDataType<?>) inputType))
        ));
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericOptionalBox(Optional<?> optional, DataType<? extends Optional<?>> type) {
        return ((DataType<T>) type).create(((Optional<T>) Optional.of(optional)).orElse((T) Optional.empty()));
    }
}
