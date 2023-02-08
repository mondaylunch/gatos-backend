package club.mondaylunch.gatos.core.graph.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Globally stores node types by name.
 */
public final class NodeTypeRegistry {
    private static final Map<String, NodeType> registry = new HashMap<>();

    /**
     * Registers a given node type to the registry.
     * @param value the node type
     * @return the node type
     */
    public static <T extends NodeType> T register(T value) {
        registry.put(value.name(), value);
        return value;
    }

    /**
     * Gets the node type registered under a name.
     * @param name the name
     * @return an optional of the node type, or empty
     */
    public static Optional<NodeType> get(String name) {
        return Optional.ofNullable(registry.get(name));
    }
}
