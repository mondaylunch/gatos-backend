package club.mondaylunch.gatos.basicnodes.end.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.models.UserData;

public class MultiplyUserDataNodeTest {

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
    public void areSettingsCorrect() {
        var settings = BasicNodes.MULTIPLY_USER_DATA.settings();
        Assertions.assertEquals(2, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
        Assertions.assertTrue(settings.containsKey("set_if_absent"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.MULTIPLY_USER_DATA);
        var inputs = node.inputs();
        Assertions.assertEquals(2, inputs.size());
        var nodeId = node.id();
        var keyInput = inputs.get("key");
        Assertions.assertEquals(nodeId, keyInput.nodeId());
        Assertions.assertEquals("key", keyInput.name());
        Assertions.assertEquals(DataType.STRING, keyInput.type());
        var valueInput = inputs.get("value");
        Assertions.assertEquals(nodeId, valueInput.nodeId());
        Assertions.assertEquals("value", valueInput.name());
        Assertions.assertEquals(DataType.NUMBER, valueInput.type());
    }

    @Test
    public void areInputsCorrectWithKeySetting() {
        var node = Node.create(BasicNodes.MULTIPLY_USER_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(1, inputs.size());
        var nodeId = node.id();
        var valueInput = inputs.get("value");
        Assertions.assertEquals(nodeId, valueInput.nodeId());
        Assertions.assertEquals("value", valueInput.name());
        Assertions.assertEquals(DataType.NUMBER, valueInput.type());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.MULTIPLY_USER_DATA);
        Assertions.assertTrue(node.outputs().isEmpty());
    }

    @Test
    public void canMultiplyData() {
        var nodeType = BasicNodes.MULTIPLY_USER_DATA;
        var userId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.NUMBER.create(2.0);
        UserData.objects.set(userId, "test_key", value);
        var multiplier = DataType.NUMBER.create(5.0);
        nodeType.compute(
            userId,
            Map.of("key", key, "value", multiplier),
            Map.of("set_if_absent", DataType.BOOLEAN.create(false))
        ).join();
        var retrievedValue = UserData.objects.get(userId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(10.0), retrievedValue.orElseThrow());
    }

    @Test
    public void canMultiplyOrSetExistentData() {
        var nodeType = BasicNodes.MULTIPLY_USER_DATA;
        var userId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.NUMBER.create(2.0);
        UserData.objects.set(userId, "test_key", value);
        var multiplier = DataType.NUMBER.create(5.0);
        nodeType.compute(
            userId,
            Map.of("key", key, "value", multiplier),
            Map.of()
        ).join();
        var retrievedValue = UserData.objects.get(userId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(10.0), retrievedValue.orElseThrow());
    }

    @Test
    public void canMultiplyOrSetNonExistentData() {
        var nodeType = BasicNodes.MULTIPLY_USER_DATA;
        var userId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var multiplier = DataType.NUMBER.create(5.0);
        nodeType.compute(
            userId,
            Map.of("key", key, "value", multiplier),
            Map.of()
        ).join();
        var retrievedValue = UserData.objects.get(userId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(multiplier, retrievedValue.orElseThrow());
    }
}
