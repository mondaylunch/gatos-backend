package club.mondaylunch.gatos.core.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//TODO automatically handle transitive conversions?
/**
 * Manages conversions between {@link DataType DataTypes}. Conversions should only be registered in here if <strong>they will never fail</strong>.
 * <p>For example, Number -> String is a reasonable conversion, as all numbers can be represented as strings. String -> Number is not,
 * as not all strings are valid numbers.</p>
 */
public final class Conversions {
    private static final Map<ConversionPair, Function<?, ?>> MAP = new HashMap<>();

    /**
     * Register a conversion between two types.
     * @param typeA                 the first DataType
     * @param typeB                 the second DataType
     * @param conversionFunction    a function to convert from A to B
     * @param <A>                   the first type
     * @param <B>                   the second type
     */
    public static <A, B> void register(DataType<A> typeA, DataType<B> typeB, Function<A, B> conversionFunction) {
        MAP.put(new ConversionPair(typeA, typeB), conversionFunction);
    }

    /**
     * Determines whether there is a conversion registered between two DataTypes.
     * @param a the first DataType
     * @param b the second DataType
     * @return  whether there is a conversion between the two
     */
    public static boolean canConvert(DataType<?> a, DataType<?> b) {
        return a.equals(b) || MAP.containsKey(new ConversionPair(a, b));
    }

    /**
     * Convert a DataBox of one type to another.
     * @param a         the DataBox to convert
     * @param typeB     the DataType to convert to
     * @param <A>       the first type
     * @param <B>       the second type
     * @return          the converted DataBox
     */
    @SuppressWarnings("unchecked")
    public static <A, B> DataBox<B> convert(DataBox<A> a, DataType<B> typeB) {
        if (a.type().equals(typeB)) {
            return (DataBox<B>) a;
        }
        var func = (Function<A, B>) MAP.get(new ConversionPair(a.type(), typeB));
        if (func == null) {
            throw new ConversionException("Cannot convert %s to %s".formatted(a.type(), typeB));
        }
        B result = func.apply(a.value());
        if (result == null) {
            throw new ConversionException("Conversion function %s -> %s on %s returned null!".formatted(a.type(), typeB, a.value()));
        }

        return typeB.create(result);
    }

    /**
     * Get a list of all registered conversions.
     * @return a list of all registered conversions
     */
    public static List<ConversionPair> getAllConversions() {
        return List.copyOf(MAP.keySet());
    }

    private Conversions() {}

    public record ConversionPair(DataType<?> a, DataType<?> b) {
    }

    /**
     * Thrown when there is an error in DataType conversion.
     */
    public static class ConversionException extends RuntimeException {
        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(Throwable cause) {
            super(cause);
        }
    }
}
