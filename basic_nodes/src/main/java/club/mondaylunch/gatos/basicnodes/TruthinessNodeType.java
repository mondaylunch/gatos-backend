package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class TruthinessNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return null;
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input_data", DataType.ANY)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN)
        );
    }

    @Override
    @SuppressWarnings("ALL")
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("input", DataType.ANY);
        var input = DataBox.get(inputs, "input_data", DataType.ANY);
        return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(input.isPresent() && this.isInherentlyTruthy(inputType, input)
            || switch (inputType.name()) {
                case "number" -> (double) input.get() != 0.0D;
                case "boolean" -> (boolean) input.get();
                case "string" -> ((String) input.get()).length() != 0;
                default -> false;
            })));
    }

    private boolean isInherentlyTruthy(DataType<?> dataType, Object input) {
        return dataType instanceof ListDataType<?>
            || (dataType instanceof OptionalDataType<?> && ((Optional<?>) input).isPresent())
            || dataType.equals(DataType.JSON_OBJECT);
    }
}
