package club.mondaylunch.gatos.core.collection.test;

import java.util.UUID;

import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.FlowData;

public class FlowDataCollectionTest {

    @BeforeEach
    void setUp() {
        this.reset();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        FlowData.objects.clear();
    }

    @Test
    public void canSetData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
    }

    @Test
    public void canSetDataTwice() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        assertSetData(id, "test", "Test data 2");
    }

    @Test
    public void canSetMultipleData() {
        var id = UUID.randomUUID();
        var data1 = DataType.STRING.create("Test data");
        var data2 = DataType.STRING.create("Test data 2");
        FlowData.objects.set(id, "test", data1);
        FlowData.objects.set(id, "test2", data2);
        var retrieved1 = FlowData.objects.get(id, "test");
        var retrieved2 = FlowData.objects.get(id, "test2");
        Assertions.assertTrue(retrieved1.isPresent());
        Assertions.assertTrue(retrieved2.isPresent());
        Assertions.assertEquals(data1, retrieved1.orElseThrow());
        Assertions.assertEquals(data2, retrieved2.orElseThrow());
    }

    @Test
    public void canSetIfAbsentExistentData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        FlowData.objects.setIfAbsent(id, "test", DataType.STRING.create("Test data 2"));
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.STRING.create("Test data"), retrieved.orElseThrow());
    }

    @Test
    public void canSetIfAbsentNonExistentData() {
        var id = UUID.randomUUID();
        var value = DataType.STRING.create("Test data");
        FlowData.objects.setIfAbsent(id, "test", value);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(value, retrieved.orElseThrow());
    }

    @Test
    public void canCheckDataType() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        Assertions.assertFalse(FlowData.objects.contains(id, "test", DataType.NUMBER));
    }

    @Test
    public void canRemoveData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        FlowData.objects.remove(id, "test");
        Assertions.assertFalse(FlowData.objects.contains(id, "test"));
        Assertions.assertFalse(FlowData.objects.contains(id, "test", DataType.STRING));
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isEmpty());
    }

    @Test
    public void canIncrementNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        FlowData.objects.increment(id, "test", 1);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementNegativeNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        FlowData.objects.increment(id, "test", -1);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(0.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementFractionalNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        FlowData.objects.increment(id, "test", 0.5);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.5), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementZero() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        FlowData.objects.increment(id, "test", 0);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.0), retrieved.orElseThrow());
    }

    @Test
    public void cannotIncrementNonNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        Assertions.assertThrows(
            MongoWriteException.class,
            () -> FlowData.objects.increment(id, "test", 1)
        );
    }

    @Test
    public void cannotIncrementNonExistentData() {
        var id = UUID.randomUUID();
        FlowData.objects.increment(id, "test", 1);
        Assertions.assertFalse(FlowData.objects.contains(id, "test"));
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isEmpty());
    }

    @Test
    public void canIncrementOrSetExistentData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        FlowData.objects.incrementOrSet(id, "test", 1);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementOrSetNonExistentData() {
        var id = UUID.randomUUID();
        FlowData.objects.incrementOrSet(id, "test", 1);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.0), retrieved.orElseThrow());
    }

    @Test
    public void canMultiplyNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 5);
        FlowData.objects.multiply(id, "test", 2);
        var retrieved = FlowData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(10.0), retrieved.orElseThrow());
    }

    private static void assertSetData(UUID flowId, String key, DataBox<?> value) {
        FlowData.objects.set(flowId, key, value);
        Assertions.assertTrue(FlowData.objects.contains(flowId, key));
        Assertions.assertTrue(FlowData.objects.contains(flowId, key, value.type()));
        var retrieved = FlowData.objects.get(flowId, key);
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(value, retrieved.orElseThrow());
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertSetData(UUID flowId, String key, String value) {
        var data = DataType.STRING.create(value);
        assertSetData(flowId, key, data);
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertSetData(UUID flowId, String key, double value) {
        var data = DataType.NUMBER.create(value);
        assertSetData(flowId, key, data);
    }
}
