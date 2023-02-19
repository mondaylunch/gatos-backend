package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class BooleanOperationNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
                "config", DataType.STRING.create("")
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        if(DataBox.get(settings, "mode", DataType.STRING).orElseThrow().equals("not"))
            return Set.of(new NodeConnector.Input<>(nodeId, "input", DataType.BOOLEAN));

        return Set.of(
                new NodeConnector.Input<>(nodeId, "inputA", DataType.BOOLEAN),
                new NodeConnector.Input<>(nodeId, "inputB", DataType.BOOLEAN)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
                new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String config = DataBox.get(settings, "mode", DataType.STRING).orElseThrow();
        if(config.equals("not"))
            return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(
                !DataBox.get(inputs, "input", DataType.BOOLEAN).orElseThrow()
            )));

        boolean a = DataBox.get(inputs, "inputA", DataType.BOOLEAN).orElseThrow();
        boolean b = DataBox.get(inputs, "inputB", DataType.BOOLEAN).orElseThrow();

        boolean result = switch(config) {
            case "or"   -> a | b;
            case "and"  -> a & b;
            case "xor"  -> a ^ b;
            default     -> false;
        };

        return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(result)));
    }
}
