package club.mondaylunch.gatos.core.collection.test;

import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.JsonObjectReference;
import club.mondaylunch.gatos.core.models.UserData;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

public class UserDataCollectionTest {

    @BeforeEach
    void setUp() {
        this.reset();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        UserData.objects.clear();
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
        UserData.objects.set(id, "test", data1);
        UserData.objects.set(id, "test2", data2);
        var retrieved1 = UserData.objects.get(id, "test");
        var retrieved2 = UserData.objects.get(id, "test2");
        Assertions.assertTrue(retrieved1.isPresent());
        Assertions.assertTrue(retrieved2.isPresent());
        Assertions.assertEquals(data1, retrieved1.orElseThrow());
        Assertions.assertEquals(data2, retrieved2.orElseThrow());
    }

    @Test
    public void canSetIfAbsentExistentData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        UserData.objects.setIfAbsent(id, "test", DataType.STRING.create("Test data 2"));
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.STRING.create("Test data"), retrieved.orElseThrow());
    }

    @Test
    public void canSetIfAbsentNonExistentData() {
        var id = UUID.randomUUID();
        var value = DataType.STRING.create("Test data");
        UserData.objects.setIfAbsent(id, "test", value);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(value, retrieved.orElseThrow());
    }

    @Test
    public void canCheckDataType() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        Assertions.assertFalse(UserData.objects.contains(id, "test", DataType.NUMBER));
    }

    @Test
    public void canRemoveData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        UserData.objects.delete(id, "test");
        Assertions.assertFalse(UserData.objects.contains(id, "test"));
        Assertions.assertFalse(UserData.objects.contains(id, "test", DataType.STRING));
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isEmpty());
    }

    @Test
    public void canRemoveAllDataForUser() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        assertSetData(id1, "test_1", "Test data 1");
        assertSetData(id1, "test_2", "Test data 2");
        assertSetData(id2, "test_3", "Test data 3");
        UserData.objects.delete(id1);
        Assertions.assertEquals(1, UserData.objects.size());
        Assertions.assertFalse(UserData.objects.contains(id1, "test_1"));
        Assertions.assertFalse(UserData.objects.contains(id1, "test_2"));
        Assertions.assertTrue(UserData.objects.contains(id2, "test_3"));
        var retrieved = UserData.objects.get(id2, "test_3");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.STRING.create("Test data 3"), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        UserData.objects.increment(id, "test", 1);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementNegativeNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        UserData.objects.increment(id, "test", -1);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(0.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementFractionalNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        UserData.objects.increment(id, "test", 0.5);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.5), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementZero() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        UserData.objects.increment(id, "test", 0);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.0), retrieved.orElseThrow());
    }

    @Test
    public void cannotIncrementNonNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", "Test data");
        Assertions.assertThrows(
            MongoWriteException.class,
            () -> UserData.objects.increment(id, "test", 1)
        );
    }

    @Test
    public void cannotIncrementNonExistentData() {
        var id = UUID.randomUUID();
        UserData.objects.increment(id, "test", 1);
        Assertions.assertFalse(UserData.objects.contains(id, "test"));
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isEmpty());
    }

    @Test
    public void canIncrementOrSetExistentData() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 1);
        UserData.objects.incrementOrSet(id, "test", 1);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrieved.orElseThrow());
    }

    @Test
    public void canIncrementOrSetNonExistentData() {
        var id = UUID.randomUUID();
        UserData.objects.incrementOrSet(id, "test", 1);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(1.0), retrieved.orElseThrow());
    }

    @Test
    public void canMultiplyNumber() {
        var id = UUID.randomUUID();
        assertSetData(id, "test", 5);
        UserData.objects.multiply(id, "test", 2);
        var retrieved = UserData.objects.get(id, "test");
        Assertions.assertTrue(retrieved.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(10.0), retrieved.orElseThrow());
    }

    @Test
    public void canStoreAllDataTypes() {
        var flowId = UUID.randomUUID();
        assertSetData(flowId, "string", DataType.STRING.create("Test data"));
        assertSetData(flowId, "number", DataType.NUMBER.create(1.0));
        assertSetData(flowId, "boolean", DataType.BOOLEAN.create(true));
        var jsonObject = new JsonObject();
        jsonObject.addProperty("string", "Test data");
        jsonObject.addProperty("number", 1.0);
        jsonObject.addProperty("boolean", true);
        var nestedJsonObject = new JsonObject();
        nestedJsonObject.addProperty("string", "Test data");
        jsonObject.add("object", nestedJsonObject);
        var nestedJsonArray = new JsonArray();
        nestedJsonArray.add("Test data");
        jsonObject.add("array", nestedJsonArray);
        assertSetData(flowId, "json_object", DataType.JSON_OBJECT.create(jsonObject));
        assertSetData(flowId, "json_array", DataType.JSON_ELEMENT.create(new JsonPrimitive("Test data")));
        assertSetData(flowId, "data_type", DataType.DATA_TYPE.create(DataType.ANY));
        assertSetData(flowId, "reference", DataType.REFERENCE.create(new JsonObjectReference(jsonObject)));
        assertSetData(flowId, "process_node_type", DataType.PROCESS_NODE_TYPE.create(TestNodeTypes.PROCESS));
    }

    private static void assertSetData(UUID flowId, String key, DataBox<?> value) {
        UserData.objects.set(flowId, key, value);
        Assertions.assertTrue(UserData.objects.contains(flowId, key));
        Assertions.assertTrue(UserData.objects.contains(flowId, key, value.type()));
        var retrieved = UserData.objects.get(flowId, key);
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
