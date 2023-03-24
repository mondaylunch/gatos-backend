package club.mondaylunch.gatos.core.graph;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;

public record GraphValidityError(@Nullable UUID relatedNode, String message) {
    public static GraphValidityError missingInput(NodeConnector.Input<?> input) {
        return new GraphValidityError(input.nodeId(), "Missing input: "+input.name());
    }

    public static GraphValidityError noStart() {
        return new GraphValidityError(null, "No start node.");
    }

    public static GraphValidityError noEnd() {
        return new GraphValidityError(null, "No path from a start node to an end node.");
    }

    public static GraphValidityError cycle() {
        return new GraphValidityError(null, "Cycle detected.");
    }

    public static <T> Collection<GraphValidityError> ensureSetting(Node node, String key, DataType<T> type, Function<T, @Nullable String> validator) {
        return DataBox.get(node.settings(), key, type)
            .map(validator)
            .map(message -> Map.of(key, new GraphValidityError(node.id(), message)))
            .orElse(Map.of())
            .values();
    }
}
