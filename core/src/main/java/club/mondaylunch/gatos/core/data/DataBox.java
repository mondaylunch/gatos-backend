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
        if (res != null && Conversions.canConvert(res.type, type)) {
            return Optional.of(Conversions.convert(res, type).value());
        }
        return Optional.empty();
    }

    /**
     * Retrieves a data value of a certain type from a map. If the value does not
     * exist or is not of the expected type, the fallback map is checked.
     *
     * @param boxes        the map
     * @param fallbackBoxes the fallback map
     * @param key         the key
     * @param type       the expected datatype
     * @param <K> the type of the key
     * @param <T> the type of the data
     * @return an Optional of the value, or empty
     */
    public static <K, T> Optional<T> get(Map<K, DataBox<?>> boxes, Map<K, DataBox<?>> fallbackBoxes, K key, DataType<T> type) {
        return get(boxes, key, type).or(() -> get(fallbackBoxes, key, type));
    }
}
