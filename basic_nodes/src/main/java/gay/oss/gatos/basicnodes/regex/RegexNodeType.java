package gay.oss.gatos.basicnodes.regex;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.graph.type.NodeType;

public class RegexNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "template", DataType.STRING.create("{}")
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "regex", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "word" , DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN)
        );
    }
    
    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return Map.of(
            "output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(
                Pattern.compile(
                    DataBox.get(inputs, "regex", DataType.STRING).orElseThrow()
                ).matcher(
                    DataBox.get(inputs, "word", DataType.STRING).orElseThrow()
                ).matches()
            ))
        );
    }
}
