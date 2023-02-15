package club.mondaylunch.gatos.core.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A type of value which can be stored in a {@link DataBox}.
 */
public final class DataType<T> {

    public static final DataType<Integer> INTEGER = DataTypeRegistry.register(new DataType<>("integer"));
    public static final DataType<Boolean> BOOLEAN = DataTypeRegistry.register(new DataType<>("boolean"));
    public static final DataType<String> STRING = DataTypeRegistry.register(new DataType<>("string"));
    public static final DataType<JsonObject> JSON_OBJECT = DataTypeRegistry.register(new DataType<>("jsonobject"));
    public static final DataType<JsonElement> JSON_ELEMENT = DataTypeRegistry.register(new DataType<>("jsonelement"));
    public static final DataType<List> LIST = DataTypeRegistry.register(new DataType<>("list"));
    private final String name;
    private DataType<Optional<T>> optionalType = null;
    private DataType<List<T>> listType = null;

    /**
     * @param name the name for the type this represents
     */
    public DataType(String name) {
        this.name = name;
    }

    /**
     * Creates a new {@link DataBox} with this type and a given value.
     * @param value the value
     * @return a data box
     */
    public DataBox<T> create(T value) {
        return new DataBox<>(value, this);
    }

    /**
     * The unique name of this data type.
     * @return the unique name of this data type.
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the DataType for a list that holds data of this type.
     * @return the DataType for a list that holds data of this type
     */
    public DataType<List<T>> listOf() {
        if (this.listType == null) {
            this.listType = new DataType<>("list$" + this.name());
        }
        return this.listType;
    }

    /**
     * Returns the DataType for an optional that holds data of this type.
     * @return the DataType for an optional that holds data of this type
     */
    public DataType<Optional<T>> optionalOf() {
        if (this.optionalType == null) {
            this.optionalType = new DataType<>("optional$" + this.name());
        }
        return this.optionalType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        var that = (DataType<?>) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return "DataType[" + "name=" + this.name + ']';
    }
}
