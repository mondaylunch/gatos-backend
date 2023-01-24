package gay.oss.gatos.core.graph.test;

import gay.oss.gatos.core.graph.data.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataTypeTest {
    @Test
    public void canCreateDataBox() {
        var box = DataType.INTEGER.create(5);
        Assertions.assertEquals(5, box.value());
    }

    @Test
    public void canMakeNewBoxWithValue() {
        var box = DataType.INTEGER.create(5);
        var newBox = box.withValue(10);
        Assertions.assertEquals(10, newBox.value());
        Assertions.assertEquals(5, box.value());
    }
}
