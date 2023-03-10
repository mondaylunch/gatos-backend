package club.mondaylunch.gatos.core.data.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;

public class DataBoxTest {
    @Test
    public void canCreateDataBox() {
        var box = DataType.NUMBER.create(5.);
        Assertions.assertEquals(5, box.value());
    }

    @Test
    public void canMakeNewBoxWithValue() {
        var box = DataType.NUMBER.create(5.);
        var newBox = box.withValue(10.);
        Assertions.assertEquals(10, newBox.value());
        Assertions.assertEquals(5, box.value());
    }

    @Test
    public void canGetValueFromMap() {
        Map<String, DataBox<?>> boxes = new HashMap<>();
        boxes.put("my_num", DataType.NUMBER.create(10.));

        var box = DataBox.get(boxes, "my_num", DataType.NUMBER);
        Assertions.assertTrue(box.isPresent());
        Assertions.assertEquals(10, box.get());
    }

    @Test
    public void missingValueFromMapGivesEmpty() {
        Map<String, DataBox<?>> boxes = new HashMap<>();

        var box = DataBox.get(boxes, "my_num", DataType.NUMBER);
        Assertions.assertFalse(box.isPresent());
    }

    @Test
    public void wrongTypeFromMapGivesEmpty() {
        Map<String, DataBox<?>> boxes = new HashMap<>();
        boxes.put("my_int", DataType.BOOLEAN.create(false)); // lies!!!

        var box = DataBox.get(boxes, "my_num", DataType.NUMBER);
        Assertions.assertFalse(box.isPresent());
    }
}
