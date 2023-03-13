package club.mondaylunch.gatos.core.graph;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

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
}
