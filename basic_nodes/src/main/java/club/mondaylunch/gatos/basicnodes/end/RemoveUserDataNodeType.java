package club.mondaylunch.gatos.basicnodes.end;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.UserData;

public class RemoveUserDataNodeType extends NodeType.End {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create("")
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            return Set.of(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        } else {
            return Set.of();
        }
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING, Predicate.not(String::isBlank)).orElseThrow();
        return CompletableFuture.runAsync(() -> UserData.objects.delete(userId, key));
    }
}
