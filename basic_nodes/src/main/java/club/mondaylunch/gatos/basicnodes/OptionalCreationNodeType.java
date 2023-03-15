package club.mondaylunch.gatos.basicnodes;

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

public class OptionalCreationNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("input", DataType.ANY);
        var optionalType = inputType == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : inputType.optionalOf();
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", optionalType)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("input", DataType.ANY);
        var optionalType = inputType == DataType.ANY ? OptionalDataType.GENERIC_OPTIONAL : inputType.optionalOf();
        var inputData = DataBox.get(inputs, "input", inputType).orElseThrow();
        return Map.of("output", CompletableFuture.completedFuture(
            this.getGenericOptionalBox(Optional.of(inputData), optionalType)
        ));
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    private <T> DataBox<T> getGenericOptionalBox(Optional<?> optional, DataType<? extends Optional<?>> type) {
        return ((DataType<T>) type).create(((Optional<T>) Optional.of(optional)).orElse((T) Optional.empty()));
    }
}
