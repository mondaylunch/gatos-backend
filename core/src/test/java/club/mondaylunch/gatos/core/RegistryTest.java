package club.mondaylunch.gatos.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryTest {
    private static final Registry<Object> TEST_REGISTRY = Registry.create("test_registry", Object.class);

    @Test
    public void canAddAndRetrieve() {
        var myThing = new Object();

        Assertions.assertEquals(myThing, TEST_REGISTRY.register("my_thing", myThing));
        var res = TEST_REGISTRY.get("my_thing");
        Assertions.assertTrue(res.isPresent());
        Assertions.assertEquals(myThing, res.get());
    }

    @Test
    public void retrievingNonexistentGivesEmptyOptional() {
        Assertions.assertFalse(TEST_REGISTRY.get("nonexistent_thing").isPresent());
    }
}
