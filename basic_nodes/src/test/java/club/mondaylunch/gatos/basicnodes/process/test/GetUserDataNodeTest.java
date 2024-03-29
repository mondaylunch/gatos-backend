package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.models.UserData;

public class GetUserDataNodeTest {

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
        var settings = BasicNodes.GET_USER_DATA.settings();
        Assertions.assertEquals(2, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
        Assertions.assertTrue(settings.containsKey("type"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.GET_USER_DATA);
        var inputs = node.inputs();
        Assertions.assertEquals(1, inputs.size());
        var nodeId = node.id();
        var keyInput = inputs.get("key");
        Assertions.assertEquals(nodeId, keyInput.nodeId());
        Assertions.assertEquals("key", keyInput.name());
        Assertions.assertEquals(DataType.STRING, keyInput.type());
    }

    @Test
    public void areInputsCorrectWithKeySetting() {
        var node = Node.create(BasicNodes.GET_USER_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(0, inputs.size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.GET_USER_DATA);
        var outputs = node.outputs();
        Assertions.assertEquals(1, outputs.size());
        var nodeId = node.id();
        var output = outputs.get("value");
        Assertions.assertNotNull(output);
        Assertions.assertEquals(nodeId, output.nodeId());
        Assertions.assertEquals("value", output.name());
        Assertions.assertEquals(DataType.ANY.optionalOf(), output.type());
    }

    @Test
    public void areOutputsCorrectWithType() {
        var node = Node.create(BasicNodes.GET_USER_DATA)
            .modifySetting("type", DataType.DATA_TYPE.create(DataType.STRING));
        var outputs = node.outputs();
        Assertions.assertEquals(1, outputs.size());
        var nodeId = node.id();
        var output = outputs.get("value");
        Assertions.assertNotNull(output);
        Assertions.assertEquals(nodeId, output.nodeId());
        Assertions.assertEquals("value", output.name());
        Assertions.assertEquals(DataType.STRING.optionalOf(), output.type());
    }

    @Test
    public void canGetValue() {
        var nodeType = BasicNodes.GET_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of(),
            Map.of()
        );
        var valueFuture = outputs.get("value");
        Assertions.assertNotNull(valueFuture);
        var expectedValue = DataType.ANY.optionalOf().create(Optional.of("Test value"));
        var retrievedValue = valueFuture.join();
        Assertions.assertEquals(expectedValue, retrievedValue);
    }

    @Test
    public void canGetNonExistentValue() {
        var nodeType = BasicNodes.GET_USER_DATA;
        var userId = UUID.randomUUID();
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of(),
            Map.of()
        );
        var valueFuture = outputs.get("value");
        Assertions.assertNotNull(valueFuture);
        var expectedValue = DataType.ANY.optionalOf().create(Optional.empty());
        var retrievedValue = valueFuture.join();
        Assertions.assertEquals(expectedValue, retrievedValue);
    }

    @Test
    public void canGetValueWithType() {
        var nodeType = BasicNodes.GET_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of("type", DataType.DATA_TYPE.create(DataType.STRING)),
            Map.of()
        );
        var valueFuture = outputs.get("value");
        Assertions.assertNotNull(valueFuture);
        var expectedValue = DataType.STRING.optionalOf().create(Optional.of("Test value"));
        var retrievedValue = valueFuture.join();
        Assertions.assertEquals(expectedValue, retrievedValue);
    }
}
