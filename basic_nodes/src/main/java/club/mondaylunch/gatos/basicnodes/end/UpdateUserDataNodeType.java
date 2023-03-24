package club.mondaylunch.gatos.basicnodes.end;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.GatosUtils;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;

public abstract class UpdateUserDataNodeType extends NodeType.End {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create(""),
            "set_if_absent", DataType.BOOLEAN.create(true)
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> flowOrGraph) {
        return GatosUtils.union(
            GraphValidityError.ensureSetting(node, "key", DataType.STRING, key -> key.isBlank() ? "Key cannot be blank" : null),
            super.isValid(node, flowOrGraph));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        Set<NodeConnector.Input<?>> inputs = new HashSet<>();
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            inputs.add(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        }
        inputs.add(new NodeConnector.Input<>(nodeId, "value", DataType.NUMBER));
        return Collections.unmodifiableSet(inputs);
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING, Predicate.not(String::isBlank)).orElseThrow();
        var value = DataBox.get(inputs, "value", DataType.NUMBER).orElseThrow();
        boolean setIfAbsent = DataBox.get(settings, "set_if_absent", DataType.BOOLEAN).orElse(true);
        return CompletableFuture.runAsync(() -> this.updateValue(userId, key, value, setIfAbsent));
    }

    protected abstract void updateValue(UUID userId, String key, double value, boolean setIfAbsent);
}
