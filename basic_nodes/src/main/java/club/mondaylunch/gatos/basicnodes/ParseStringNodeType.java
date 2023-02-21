package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ParseStringNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.NUMBER));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var inputString = DataBox.get(inputs, "input", DataType.STRING).orElse("");
        double result = 0;
        if(inputString.contains(",")) {
            var groups = inputString.split(",");
            var j = groups.length - 1;
            for(int i = 0; i <= j; i++) result += Double.parseDouble(groups[j - i]) * Math.pow(10, i*3);
        }
        else result = Double.parseDouble(inputString);
        
        return Map.of(
            "output", CompletableFuture.completedFuture(DataType.NUMBER.create(result))
        );
    }
}
