package club.mondaylunch.gatos.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GatosUtils {
    /**
     * Maps the keys and values of a map.
     * @param map           the map
     * @param keyMapper     the function to map the keys
     * @param valueMapper   the function to map the values
     * @return              the transformed map
     */
    @SuppressWarnings("unused")
    public static <K, V, K1, V1> Map<K1, V1> mapMap(Map<K, V> map, Function<K, K1> keyMapper, Function<V, V1> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(
            e -> keyMapper.apply(e.getKey()),
            e -> valueMapper.apply(e.getValue())
        ));
    }

    /**
     * Performs the union operation on some collections.
     * @param collections   the collections
     * @return              a set containing all the elements of all the collections
     */
    @SafeVarargs
    public static <E> Set<E> union(Collection<E>... collections) {
        return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
