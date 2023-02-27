package club.mondaylunch.gatos.basicnodes;

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

public class HTTPRequestNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "url", DataType.STRING.create(""),
            "method", DataType.STRING.create("")
        );
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "body", DataType.STRING));
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "StatusCode", DataType.NUMBER),
            new NodeConnector.Output<>(nodeId, "responseText", DataType.STRING)
            );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var url = DataBox.get(settings, "url", DataType.STRING).orElse("");
        var method = DataBox.get(settings, "method", DataType.STRING).orElse("");

        return Map.of(
            "StatusCode", CompletableFuture.completedFuture(DataType.NUMBER.create(200.0)),
            "responseText", CompletableFuture.completedFuture(DataType.STRING.create("among"))
        );
    }
    
}
