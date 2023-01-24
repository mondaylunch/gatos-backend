package gay.oss.gatos.core.graph.data;

/**
 * Holds (boxes) a value and a reference to its type.
 * @param value the value stored
 * @param type  the {@link DataType} of the value stored
 * @param <T>   the type of the data stored
 */
public record DataBox<T>(T value, DataType<T> type) {
    /**
     * Creates a new instance the same as this one, but with the given value.
     *
     * @param value the value
     * @return the new instance
     */
    public DataBox<T> withValue(T value) {
        return new DataBox<>(value, this.type);
    }
}
