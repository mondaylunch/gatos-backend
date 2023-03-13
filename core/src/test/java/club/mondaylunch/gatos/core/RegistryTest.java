package club.mondaylunch.gatos.core;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegistryTest {
    private static final Registry<Foo> FOO_REGISTRY = Registry.create("foo", Foo.class);

    @BeforeEach
    public void resetRegistries() {
        FOO_REGISTRY.clear();
    }

    @Test
    public void testRegistryProperlyCreated() {
        Assertions.assertEquals(Foo.class, FOO_REGISTRY.getClazz());
        Assertions.assertEquals(FOO_REGISTRY, Registry.getRegistryByClass(Foo.class).orElseThrow());
        Assertions.assertEquals(FOO_REGISTRY, Registry.REGISTRIES.get("foo").orElseThrow());
        Assertions.assertEquals("foo", Registry.REGISTRIES.getName(FOO_REGISTRY).orElse(null));
    }

    @Test
    public void testRegisterAndGet() {
        Foo foo = new Foo("test");
        FOO_REGISTRY.register("test_foo", foo);
        Optional<Foo> retrievedFoo = FOO_REGISTRY.get("test_foo");
        Assertions.assertTrue(retrievedFoo.isPresent());
        Assertions.assertEquals(foo, retrievedFoo.get());
    }

    @Test
    public void testDuplicateRegistration() {
        Foo foo = new Foo("test");
        FOO_REGISTRY.register("test_foo", foo);
        Assertions.assertThrows(IllegalArgumentException.class, () -> FOO_REGISTRY.register("test_foo_to", foo));
    }

    @Test
    public void testDuplicateNameRegistration() {
        Foo foo1 = new Foo("test_1");
        Foo foo2 = new Foo("test_2");
        FOO_REGISTRY.register("test_foo", foo1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> FOO_REGISTRY.register("test_foo", foo2));
    }

    @Test
    public void testGetName() {
        Foo foo = new Foo("test");
        FOO_REGISTRY.register("test_foo", foo);
        Optional<String> name = FOO_REGISTRY.getName(foo);
        Assertions.assertTrue(name.isPresent());
        Assertions.assertEquals("test_foo", name.get());
    }

    @Test
    public void testGetEntries() {
        Foo foo = new Foo("test");
        FOO_REGISTRY.register("test_foo", foo);
        Assertions.assertTrue(FOO_REGISTRY.getEntries().contains(Map.entry("test_foo", foo)));
    }

    @Test
    public void testGetValues() {
        Foo foo = new Foo("test");
        FOO_REGISTRY.register("test_foo", foo);
        Assertions.assertTrue(FOO_REGISTRY.getValues().contains(foo));
    }

    private record Foo(String name) {}
}
