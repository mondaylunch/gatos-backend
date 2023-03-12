package club.mondaylunch.gatos.core.data;

import java.util.Optional;

public final class OptionalDataType<T> extends DataType<Optional<T>> {
    public static final String PREFIX = "optional$";
    public static final DataType<Optional<?>> GENERIC_OPTIONAL = DataType.register("optional", Optional.class);
    private final DataType<T> contains;

    /**
     * @param contains the datatype that this optional will contain
     */
    public OptionalDataType(DataType<T> contains) {
        super(makeName(contains), Optional.class);
        this.contains = contains;
        Conversions.register(this, GENERIC_OPTIONAL, $ -> $);
    }

    /**
     * Creates the name for the optional type that holds a given type.
     * @param type  the type
     * @return      the name of an optional type for the type
     */
    public static String makeName(DataType<?> type) {
        return PREFIX+type.name();
    }

    /**
     * Gets the datatype this datatype is specialised for.
     * @return the datatype held by optionals of this type
     */
    public DataType<T> contains() {
        return this.contains;
    }
}
