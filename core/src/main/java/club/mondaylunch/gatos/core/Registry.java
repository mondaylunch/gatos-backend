package club.mondaylunch.gatos.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class Registry<T> {
    public static final Registry<Registry<?>> REGISTRIES = new Registry<>(Registry.class);
    private final Map<String, T> map = new HashMap<>();
    private final Map<T, String> reverseMap = new HashMap<>();
    private final Class<? super T> clazz;

    private Registry(Class<? super T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Create and register a new registry.
     * @param name  the name of the registry
     * @param clazz the class of the objects this registry will hold
     * @param <T>   the type of the objects this registry will hold
     * @return      the new registry
     */
    public static <T> Registry<T> create(String name, Class<? super T> clazz) {
        return REGISTRIES.register(name, new Registry<>(clazz));
    }

    /**
     * Registers an object in this registry.
     * @param name  the name to register this object under
     * @param value the object to register
     * @param <E>   the type of the object
     * @return      the object
     * @throws IllegalStateException if the object is already registered, or something is already registered with the name
     */
    public <E extends T> E register(@NotNull String name, @NotNull E value) {
        if (this.map.containsKey(name)) {
            throw new IllegalArgumentException("Tried to register %s (%s) but something with that name (%s) already exists!".formatted(name, value, this.map.get(name)));
        }
        if (this.reverseMap.containsKey(value)) {
            throw new IllegalArgumentException("Tried to register %s @ %s but it already exists @ %s!".formatted(value, name, this.reverseMap.get(value)));
        }
        this.map.put(name, value);
        this.reverseMap.put(value, name);
        return value;
    }

    /**
    * Gets an object registered in this registry by name.
    * @param name the name of the object
    * @return an optional of the object, or empty if no object is registered with that name
    */
    public Optional<T> get(@NotNull String name) {
        return Optional.ofNullable(this.map.get(name));
    }

    /**
     * Gets the name of an object registered in this registry.
     * @param value the object
     * @return an optional of the name, or empty if the object is not registered
     */
    public Optional<String> getName(@NotNull T value) {
        return Optional.ofNullable(this.reverseMap.get(value));
    }

    /**
     * Gets a set of map entries of the registry.
     * This set may or may not be modifiable. It will not update with the registry.
     * @return a set of registry entries
     */
    public Set<Map.Entry<String, T>> getEntries() {
        return Map.copyOf(this.map).entrySet();
    }

    /**
     * Gets a set of registered objects in the registry.
     * This set may or may not be modifiable. It will not update with the registry.
     * @return a set of registered objects
     */
    public Set<T> getValues() {
        return Set.copyOf(this.map.values());
    }

    /**
     * Gets the class of objects this registry is for.
     * @return the class this registry is for
     */
    public Class<? super T> getClazz() {
        return this.clazz;
    }

    /**
     * Clears this registry, making it empty. You probably only want to use this in tests.
     */
    public void clear() {
        this.map.clear();
        this.reverseMap.clear();
    }

    /**
     * Find a registry that holds values of a given class.
     * @param clazz the class
     * @param <T>   the type
     * @return      a registry that holds the type given, or empty
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Optional<Registry<R>> getRegistryByClass(Class<T> clazz) {
        return REGISTRIES.getValues().stream()
            .filter(r -> r.getClazz().isAssignableFrom(clazz))
            .map(r -> (Registry<R>) r)
            .findAny();
    }
}
