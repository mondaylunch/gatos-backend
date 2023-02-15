package club.mondaylunch.gatos.core.data;

import java.util.List;

public final class ListDataType<T> extends DataType<List<T>> {
    private final DataType<T> contains;

    /**
     * @param contains the datatype that this list will contain
     */
    public ListDataType(DataType<T> contains) {
        super(makeName(contains));
        this.contains = contains;
    }

    /**
     * Creates the name for the list type that holds a given type
     * @param type  the type
     * @return      the name of a list type for the type
     */
    public static String makeName(DataType<?> type) {
        return "list$"+type.name();
    }

    /**
     * Gets the datatype this datatype is specialised for
     * @return the datatype held by lists of this type
     */
    public DataType<T> contains() {
        return this.contains;
    }
}