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

public class OptionalOrElseNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        DataType<?> type = this.getTypeFromInputs(inputTypes);
        DataType<?> optionalDataType = type == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : type.optionalOf();
        return Set.of(
            new NodeConnector.Input<>(nodeId, "optional", optionalDataType),
            new NodeConnector.Input<>(nodeId, "fallback", type)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        DataType<?> type = this.getTypeFromInputs(inputTypes);
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", type)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        DataType<?> type = this.getTypeFromInputs(inputTypes);
        DataType<?> optionalDataType = type == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : type.optionalOf();
        Optional<?> optional = (Optional<?>) DataBox.get(inputs, "optional", optionalDataType).orElseThrow();
        Object fallback = DataBox.get(inputs, "fallback", type).orElseThrow();
        var result = this.getValue(optional, fallback, type);
        return Map.of(
            "output", CompletableFuture.completedFuture(result)
        );
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getValue(Optional<?> optional, Object fallback, DataType<?> type) {
        return ((DataType<T>) type).create(((Optional<T>) optional).orElse((T) fallback));
    }

    private DataType<?> getTypeFromInputs(Map<String, DataType<?>> inputTypes) {
        if (inputTypes.get("optional") instanceof OptionalDataType<?> optType) {
            return optType.contains();
        } else {
            return inputTypes.getOrDefault("fallback", DataType.ANY);
        }
    }
}
