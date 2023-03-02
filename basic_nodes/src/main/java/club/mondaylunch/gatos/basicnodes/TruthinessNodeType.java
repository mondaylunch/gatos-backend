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
        return Map.of();
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
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputType = inputTypes.getOrDefault("input", DataType.ANY);
        // NB: "OptionalDataType.GENERIC_OPTIONAL instanceof OptionalDataType" is false
        var input = inputType instanceof OptionalDataType<?>
            ? (Optional<?>) DataBox.get(inputs, "input_data", inputType).orElseThrow()
            : DataBox.get(inputs, "input_data", inputType);
        return Map.of("output", CompletableFuture.completedFuture(
            DataType.BOOLEAN.create(isInherentlyTruthy(inputType) || input.map(value -> evaluateValue(evaluateExactType(inputType), value)).orElse(false))));
    }

    private static DataType<?> evaluateExactType(DataType<?> dataType) {
        return dataType instanceof OptionalDataType<?> opt
            ? opt.contains()
            : dataType;
    }

    private static boolean isInherentlyTruthy(DataType<?> dataType) {
        // NB: "ListDataType.GENERIC_LIST instanceof ListDataType" is false
        return dataType instanceof ListDataType<?> || dataType.equals(DataType.JSON_OBJECT);
    }

    private static boolean evaluateValue(DataType<?> dataType, Object value) {
        return switch (dataType.name()) {
            case "number" -> (double) value != 0;
            case "boolean" -> (boolean) value;
            case "string" -> ((String) value).length() != 0;
            default -> false;
        };
    }
}
