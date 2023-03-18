package club.mondaylunch.gatos.core.data;

import java.util.List;

public final class ListDataType<T> extends DataType<List<T>> {
    public static final String PREFIX = "list$";
    @SuppressWarnings("unchecked")
    public static final DataType<List<?>> GENERIC_LIST = (DataType<List<?>>) (DataType<?>) DataType.ANY.listOf();
    private final DataType<T> contains;

    /**
     * @param contains the datatype that this list will contain
     */
    public ListDataType(DataType<T> contains, boolean registerSimpleConversion) {
        super(makeName(contains), List.class, registerSimpleConversion);
        this.contains = contains;
    }

    /**
     * Creates the name for the list type that holds a given type.
     * @param type  the type
     * @return      the name of a list type for the type
     */
    public static String makeName(DataType<?> type) {
        return PREFIX+type.name();
    }

    /**
     * Gets the datatype this datatype is specialised for.
     * @return the datatype held by lists of this type
     */
    public DataType<T> contains() {
        return this.contains;
    }
}
