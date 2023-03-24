package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ParseStringToNumberNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.NUMBER));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        double output;
        try {
            output = Double.parseDouble(String.join("", DataBox.get(inputs, "input", DataType.STRING).orElse("").split(",")));
        } catch (NumberFormatException e) {
            output = Double.NaN;
        }
        return Map.of(
            "output", CompletableFuture.completedFuture(DataType.NUMBER.create(output)));
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs) {
        return this.compute(UUID.randomUUID(), inputs, Map.of(), Map.of());
    }
}
