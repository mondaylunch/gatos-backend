package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ListLengthNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.LIST));
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.INTEGER));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var inputList = DataBox.get(inputs, "input", DataType.LIST).orElse(new ArrayList<>());
        return Map.of("output", CompletableFuture.completedFuture(DataType.INTEGER.create(inputList.size())));
    }

}
