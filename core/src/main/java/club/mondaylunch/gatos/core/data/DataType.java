package club.mondaylunch.gatos.core.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import club.mondaylunch.gatos.core.Registry;
import club.mondaylunch.gatos.core.graph.type.NodeCategory;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.JsonObjectReference;

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
    public static final DataType<DataType<?>> DATA_TYPE = register("data_type", DataType.class);
    public static final DataType<JsonObjectReference> REFERENCE = register("reference", JsonObjectReference.class);
    public static final DataType<NodeType.Process> PROCESS_NODE_TYPE = register("process_node_type", NodeType.class);

    static {
        // Conversions to string
        Conversions.register(ANY, STRING, Object::toString);
        Conversions.register(DATA_TYPE, STRING, DataType::name);
        Conversions.register(PROCESS_NODE_TYPE, STRING, n -> NodeType.REGISTRY.getName(n).orElse(n.toString()));

        // Conversions to JSON element
        Conversions.register(NUMBER, JSON_ELEMENT, JsonPrimitive::new);
        Conversions.register(BOOLEAN, JSON_ELEMENT, JsonPrimitive::new);
        Conversions.register(STRING, JSON_ELEMENT, JsonPrimitive::new);
        Conversions.register(JSON_OBJECT, JSON_ELEMENT, $ -> $);
        Conversions.registerSimple(JSON_ELEMENT.listOf(), JSON_ELEMENT, elements -> elements.stream().collect(
            JsonArray::new,
            JsonArray::add,
            JsonArray::addAll
        ));
        Conversions.registerSimple(JSON_ELEMENT.optionalOf(), JSON_ELEMENT, optional -> optional.orElse(JsonNull.INSTANCE));

        // Widgets
        SettingWidgets.register(NUMBER, SettingWidgets.Widget.NUMBERBOX);
        SettingWidgets.register(BOOLEAN, SettingWidgets.Widget.CHECKBOX);
        SettingWidgets.register(STRING, SettingWidgets.Widget.TEXTBOX);
        SettingWidgets.register(JSON_OBJECT, SettingWidgets.Widget.TEXTAREA);
        SettingWidgets.register(DATA_TYPE, SettingWidgets.Widget.dropdown(u ->
            DataType.REGISTRY.getEntries().stream().map(Map.Entry::getKey).toList()));
        SettingWidgets.register(PROCESS_NODE_TYPE, SettingWidgets.Widget.dropdown(u ->
            NodeType.REGISTRY.getEntries().stream()
                .filter(kv -> kv.getValue().category() == NodeCategory.PROCESS)
                .map(Map.Entry::getKey).toList()));
    }

    private final String name;
    private final Class<? super T> clazz;

    /**
     * Creates a new DataType.
     *
     * @param name  the name for the type this represents
     * @param clazz the class for this type
     */
    protected DataType(String name, Class<? super T> clazz) {
        this.name = name;
        this.clazz = clazz;
        if (!Objects.equals(name, "any")) {
            Conversions.registerSimple(this, ANY, $ -> $);
        }
    }

    public static <T> DataType<T> register(String name, Class<? super T> clazz) {
        return REGISTRY.register(name, new DataType<>(name, clazz));
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
            var newListType = new ListDataType<>(this);
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
            var newOptionalType = new OptionalDataType<>(this);
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
