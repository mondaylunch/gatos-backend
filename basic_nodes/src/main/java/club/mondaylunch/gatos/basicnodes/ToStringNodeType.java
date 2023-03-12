package club.mondaylunch.gatos.basicnodes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ToStringNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "data", DataType.ANY)
        );
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.STRING));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var input = DataBox.get(inputs, "data", DataType.ANY).orElse("");
        String result = this.canNotAutoCast(input) ? this.castToString(input) : input.toString();
        return Map.of("output", CompletableFuture.completedFuture(DataType.STRING.create(result)));
    }

    private String castToString(Object input) {
        return input instanceof Optional ?
            this.castOptional((Optional<?>) input) : this.castList((List<?>) input);
    }

    private String castOptional(Optional<?> op) {
        return op.isEmpty() ? "" : op.get().toString();
    }

    private String castList(List<?> l) {
        return l.toString();
    }

    private boolean canNotAutoCast(Object input) {
        return input instanceof Optional || input instanceof List;
    }
}
