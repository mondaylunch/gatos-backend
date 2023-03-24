package club.mondaylunch.gatos.basicnodes.end;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.UserData;

public class SetUserDataNodeType extends NodeType.End {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create(""),
            "overwrite", DataType.BOOLEAN.create(true)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        Set<NodeConnector.Input<?>> inputs = new HashSet<>();
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            inputs.add(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        }
        inputs.add(new NodeConnector.Input<>(nodeId, "value", DataType.ANY));
        return Collections.unmodifiableSet(inputs);
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING, Predicate.not(String::isBlank)).orElseThrow();
        var value = inputs.get("value");
        Objects.requireNonNull(value, "No value input");
        boolean overwrite = DataBox.get(settings, "overwrite", DataType.BOOLEAN).orElse(true);
        return CompletableFuture.runAsync(() -> {
            if (overwrite) {
                UserData.objects.set(userId, key, value);
            } else {
                UserData.objects.setIfAbsent(userId, key, value);
            }
        });
    }
}
