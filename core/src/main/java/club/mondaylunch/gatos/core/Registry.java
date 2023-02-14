package club.mondaylunch.gatos.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class Registry<T> {
    public static final RegistryRegistry REGISTRIES = new RegistryRegistry();
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
     * A registry that holds registries, that also allows lookup by class.
     */
    public static class RegistryRegistry extends Registry<Registry<?>> {
        private final Map<Class<?>, Registry<?>> registriesByClass = new HashMap<>();

        private RegistryRegistry() {
            super(Registry.class);
        }

        @Override
        public <E extends Registry<?>> E register(@NotNull String name, @NotNull E value) {
            this.registriesByClass.put(value.getClazz(), value);
            return super.register(name, value);
        }

        /**
         * Gets a registry by the class of the objects it holds.
         * @param clazz the class held by the wanted registry
         * @param <E>   the type held by the wanted registry
         * @return      the registry that holds objects of the given class
         */
        @SuppressWarnings("unchecked")
        public <E> Optional<Registry<E>> getByClass(Class<E> clazz) {
            return Optional.ofNullable((Registry<E>) this.registriesByClass.get(clazz));
        }
    }
}
