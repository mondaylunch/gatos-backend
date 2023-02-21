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
}
