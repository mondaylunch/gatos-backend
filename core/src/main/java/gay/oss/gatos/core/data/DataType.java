package gay.oss.gatos.core.data;

/**
 * A type of value which can be stored in a {@link DataBox}.
 * @param clazz the class for the data this represents
 * @param name  the name for the data this represents
 * @param <T>   the type of data this represents
 */
public record DataType<T>(Class<T> clazz, String name) {
    public static final DataType<Integer> INTEGER = new DataType<>(Integer.class, "integer");
    public static final DataType<Boolean> BOOLEAN = new DataType<>(Boolean.class, "boolean");
    public static final DataType<String> STRING = new DataType<>(String.class, "string");

    /**
     * Creates a new {@link DataBox} with this type and a given value.
     * @param value the value
     * @return      a data box
     */
    public DataBox<T> create(T value) {
        return new DataBox<>(value, this);
    }
}
