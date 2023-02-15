package club.mondaylunch.gatos.core;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegistryTest {
    private Registry<Foo> fooRegistry;

    @BeforeEach
    public void resetRegistries() {
        Registry.REGISTRIES.clear();
        this.fooRegistry = Registry.create("foo", Foo.class);
    }

    @Test
    public void testRegistryProperlyCreated() {
        Assertions.assertEquals(Foo.class, this.fooRegistry.getClazz());
        Assertions.assertEquals(this.fooRegistry, Registry.REGISTRIES.getByClass(Foo.class).orElseThrow());
        Assertions.assertEquals(this.fooRegistry, Registry.REGISTRIES.get("foo").orElseThrow());
        Assertions.assertEquals("foo", Registry.REGISTRIES.getName(this.fooRegistry).orElse(null));
    }

    @Test
    public void testRegisterAndGet() {
        Foo foo = new Foo("test");
        this.fooRegistry.register("test_foo", foo);
        Optional<Foo> retrievedFoo = this.fooRegistry.get("test_foo");
        Assertions.assertTrue(retrievedFoo.isPresent());
        Assertions.assertEquals(foo, retrievedFoo.get());
    }

    @Test
    public void testDuplicateRegistration() {
        Foo foo = new Foo("test");
        this.fooRegistry.register("test_foo", foo);
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.fooRegistry.register("test_foo_to", foo));
    }

    @Test
    public void testDuplicateNameRegistration() {
        Foo foo1 = new Foo("test_1");
        Foo foo2 = new Foo("test_2");
        this.fooRegistry.register("test_foo", foo1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.fooRegistry.register("test_foo", foo2));
    }

    @Test
    public void testGetName() {
        Foo foo = new Foo("test");
        this.fooRegistry.register("test_foo", foo);
        Optional<String> name = this.fooRegistry.getName(foo);
        Assertions.assertTrue(name.isPresent());
        Assertions.assertEquals("test_foo", name.get());
    }

    @Test
    public void testGetEntries() {
        Foo foo = new Foo("test");
        this.fooRegistry.register("test_foo", foo);
        Assertions.assertTrue(this.fooRegistry.getEntries().contains(Map.entry("test_foo", foo)));
    }

    @Test
    public void testGetValues() {
        Foo foo = new Foo("test");
        this.fooRegistry.register("test_foo", foo);
        Assertions.assertTrue(this.fooRegistry.getValues().contains(foo));
    }

    private record Foo(String name) {}
}
