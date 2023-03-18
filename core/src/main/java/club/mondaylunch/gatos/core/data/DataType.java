package club.mondaylunch.gatos.core.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import club.mondaylunch.gatos.core.Registry;

/**
 * A type of value which can be stored in a {@link DataBox}.
 */
public sealed class DataType<T> permits ListDataType, OptionalDataType {

    public static final DataTypeRegistry REGISTRY = Registry.REGISTRIES.register("data_type", new DataTypeRegistry());

    public static final DataType<Object> ANY = register("any", Object.class);
    public static final DataType<Double> NUMBER = register("number", Double.class);
    public static final DataType<Boolean> BOOLEAN = register("boolean", Boolean.class);
    public static final DataType<String> STRING = register("string", String.class);
    public static final DataType<JsonObject> JSON_OBJECT = register("json_object", JsonObject.class);
    public static final DataType<JsonElement> JSON_ELEMENT = register("json_element", JsonElement.class);
    public static final DataType<AtomicReference<?>> REFERENCE = register("reference", AtomicReference.class);

    static {
        Conversions.register(ANY, STRING, Object::toString);
    }

    private final String name;
    private final Class<? super T> clazz;

    /**
     * Creates a new DataType.
     *
     * @param name                     the name for the type this represents
     * @param clazz                    the class for this type
     * @param registerSimpleConversion {@code true} if {@link Conversions#registerSimple}
     *                                 should be used, {@code false} if {@link Conversions#register}
     *                                 should be used
     */
    protected DataType(String name, Class<? super T> clazz, boolean registerSimpleConversion) {
        this.name = name;
        this.clazz = clazz;
        if (!Objects.equals(name, "any")) {
            if (registerSimpleConversion) {
                Conversions.registerSimple(this, ANY, $ -> $);
            } else {
                Conversions.register(this, ANY, $ -> $);
            }
        }
    }

    public static <T> DataType<T> register(String name, Class<? super T> clazz) {
        return REGISTRY.register(name, new DataType<>(name, clazz, false));
    }

    /**
     * Creates a new {@link DataBox} with this type and a given value.
     *
     * @param value the value
     * @return a data box
     */
    public DataBox<T> create(T value) {
        return new DataBox<>(value, this);
    }

    /**
     * The unique name of this data type.
     *
     * @return the unique name of this data type
     */
    public String name() {
        return this.name;
    }

    /**
     * The class of this data type.
     *
     * @return the class of this data type
     */
    public Class<? super T> clazz() {
        return this.clazz;
    }

    /**
     * Returns the DataType for a list that holds data of this type.
     *
     * @return the DataType for a list that holds data of this type
     */
    @SuppressWarnings("unchecked")
    public DataType<List<T>> listOf() {
        String listName = ListDataType.makeName(this);
        Optional<DataType<?>> listType = REGISTRY.getWithoutGenerating(listName);
        if (listType.isPresent()) {
            return (DataType<List<T>>) listType.get();
        } else {
            var newListType = new ListDataType<>(this, true);
            REGISTRY.register(newListType.name(), newListType);
            return newListType;
        }
    }

    /**
     * Returns the DataType for an optional that holds data of this type.
     *
     * @return the DataType for an optional that holds data of this type
     */
    @SuppressWarnings("unchecked")
    public DataType<Optional<T>> optionalOf() {
        String optionalName = OptionalDataType.makeName(this);
        Optional<DataType<?>> optionalType = REGISTRY.getWithoutGenerating(optionalName);
        if (optionalType.isPresent()) {
            return (DataType<Optional<T>>) optionalType.get();
        } else {
            var newOptionalType = new OptionalDataType<>(this, true);
            REGISTRY.register(newOptionalType.name(), newOptionalType);
            return newOptionalType;
        }
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
