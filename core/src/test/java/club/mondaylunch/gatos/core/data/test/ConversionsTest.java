package club.mondaylunch.gatos.core.data.test;

import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConversionsTest {
    private static final DataType<Foo> FOO_TYPE = DataType.register("foo");
    private static final DataType<Bar> BAR_TYPE = DataType.register("bar");
    private static final DataType<Baz> BAZ_TYPE = DataType.register("baz");

    @Test
    public void canRegisterConversion() {
        Assertions.assertDoesNotThrow(() -> {
            Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        });
    }

    @Test
    public void canConvertIsCorrect() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        Assertions.assertTrue(Conversions.canConvert(FOO_TYPE, BAR_TYPE));
        Assertions.assertFalse(Conversions.canConvert(BAR_TYPE, FOO_TYPE));
    }

    @Test
    public void canConvertToSelf() {
        Assertions.assertTrue(Conversions.canConvert(FOO_TYPE, FOO_TYPE));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        Assertions.assertEquals(foo, Conversions.convert(foo, FOO_TYPE));
    }

    @Test
    public void testConversion() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        var bar = BAR_TYPE.create(new Bar("hello!"));
        Assertions.assertEquals(bar, Conversions.convert(foo, BAR_TYPE));
    }

    @Test
    public void incorrectConversionFails() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        var bar = BAR_TYPE.create(new Bar("hello!"));
        Assertions.assertThrows(Conversions.ConversionException.class, () -> {
            Conversions.convert(bar, FOO_TYPE);
        });
    }

    @Test
    public void nullConversionFails() {
        Conversions.register(FOO_TYPE, BAZ_TYPE, foo -> null);
        var foo = FOO_TYPE.create(new Foo("hello!"));
        Assertions.assertThrows(Conversions.ConversionException.class, () -> {
            Conversions.convert(foo, BAZ_TYPE);
        });
    }

    private record Foo(String name) {}
    private record Bar(String name) {}
    private record Baz(String name) {}
}
