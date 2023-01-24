package gay.oss.gatos.core.graph;

/**
 * A box for a node setting value, which is aware of its type.
 * @param <T>   the type of data this holds
 */
public abstract class NodeSetting<T> {
    private final T value;

    protected NodeSetting(T value) {
        this.value = value;
    }

    /**
     * Returns the value stored in this setting.
     * @return  the value stored in this setting
     */
    public final T value() {
        return this.value;
    }

    /**
     * Returns the class of the data which can be stored in this setting.
     * @return  the class of what this setting holds
     */
    public abstract Class<T> getTypeClass();

    /**
     * Returns the name of the type of data which can be stored in this setting. Used for communication with the frontend.
     * @return  the name of what this setting holds
     */
    public abstract String getTypeName();

    /**
     * Creates a new setting instance the same as this one, but with the given value.
     * @param value the value
     * @return      the new setting
     */
    public abstract NodeSetting<T> withValue(T value);
}
