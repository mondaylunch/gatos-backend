package club.mondaylunch.gatos.core.data.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;

public class DataTypeTest {
    @Test
    public void canUseListType() {
        var type = DataType.NUMBER;
        var listType = type.listOf();
        Assertions.assertInstanceOf(ListDataType.class, listType);
        Assertions.assertEquals(type, ((ListDataType<Double>) listType).contains());
        Assertions.assertEquals(listType, type.listOf());
    }

    @Test
    public void canUseOptionalType() {
        var type = DataType.NUMBER;
        var optionalOf = type.optionalOf();
        Assertions.assertInstanceOf(OptionalDataType.class, optionalOf);
        Assertions.assertEquals(type, ((OptionalDataType<Double>) optionalOf).contains());
        Assertions.assertEquals(optionalOf, type.optionalOf());
    }

    @Test
    public void optionalsAreGeneratedFromRegistryGets() {
        class Foo {
        }

        var type = DataType.register("optional_generation_foo", Foo.class);
        var optionalType = DataType.REGISTRY.get(OptionalDataType.makeName(type));
        Assertions.assertTrue(optionalType.isPresent());
        Assertions.assertEquals(type.optionalOf(), optionalType.get());
    }

    @Test
    public void listsAreGeneratedFromRegistryGets() {
        class Foo {
        }

        var type = DataType.register("list_generation_foo", Foo.class);
        var listType = DataType.REGISTRY.get(ListDataType.makeName(type));
        Assertions.assertTrue(listType.isPresent());
        Assertions.assertEquals(type.listOf(), listType.get());
    }

    @Test
    public void derivedTypesAreNotGeneratedFromSpecialRegistryGet() {
        class Foo {
        }

        var type = DataType.register("no_generation_foo", Foo.class);
        var listType = DataType.REGISTRY.getWithoutGenerating(ListDataType.makeName(type));
        var optionalType = DataType.REGISTRY.getWithoutGenerating(OptionalDataType.makeName(type));
        Assertions.assertFalse(listType.isPresent());
        Assertions.assertFalse(optionalType.isPresent());
    }
}
