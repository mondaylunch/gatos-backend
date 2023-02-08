package club.mondaylunch.gatos.core.data;

import java.util.Map;
import java.util.Optional;

/**
 * Holds (boxes) a value and a reference to its type.
 * @param value the value stored
 * @param type  the {@link DataType} of the value stored
 * @param <T>   the type of the data stored
 */
public record DataBox<T>(T value, DataType<T> type) {
    /**
     * Creates a new instance the same as this one, but with the given value.
     * @param value the value
     * @return the new instance
     */
    public DataBox<T> withValue(T value) {
        return new DataBox<>(value, this.type);
    }

    /**
     * Retrieves a data value of a certain type from a map. Returns an empty
     * optional if the
     * value does not exist or is not of the expected type.
     * @param boxes the map
     * @param key   the key
     * @param type  the expected datatype
     * @param <K>   the type of the key
     * @param <T>   the type of the data
     * @return an Optional of the value, or empty
     */
    public static <K, T> Optional<T> get(Map<K, DataBox<?>> boxes, K key, DataType<T> type) {
        var res = boxes.get(key);
        if (res != null && res.type == type) {
            // noinspection unchecked
            return Optional.of((T) res.value());
        }
        return Optional.empty();
    }
}
