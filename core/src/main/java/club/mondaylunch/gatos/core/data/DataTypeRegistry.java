package club.mondaylunch.gatos.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataTypeRegistry {

    private static final Map<String, DataType<?>> registry = new HashMap<>();

    /**
     * Registers a given node type to the registry.
     *
     * @param value the data type
     * @return the data type
     */
    public static <T> DataType<T> register(DataType<T> value) {
        registry.put(value.name(), value);
        return value;
    }

    /**
     * Gets the data type registered under a name.
     *
     * @param name the name
     * @return an optional of the data type, or empty
     */
    public static Optional<DataType<?>> get(String name) {
        return Optional.ofNullable(registry.get(name));
    }
}
