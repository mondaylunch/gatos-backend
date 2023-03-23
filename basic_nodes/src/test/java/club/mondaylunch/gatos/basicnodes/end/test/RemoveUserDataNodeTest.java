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

public class RemoveUserDataNodeTest {

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
        var settings = BasicNodes.REMOVE_USER_DATA.settings();
        Assertions.assertEquals(1, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.REMOVE_USER_DATA);
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
        var node = Node.create(BasicNodes.REMOVE_USER_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(0, inputs.size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.REMOVE_USER_DATA);
        Assertions.assertTrue(node.outputs().isEmpty());
    }

    @Test
    public void canRemoveData() {
        var nodeType = BasicNodes.REMOVE_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of()
        ).join();
        var retrievedValue = UserData.objects.get(userId, "test_key");
        Assertions.assertTrue(retrievedValue.isEmpty());
    }

    @Test
    public void canRemoveNonExistentData() {
        var nodeType = BasicNodes.REMOVE_USER_DATA;
        var userId = UUID.randomUUID();
        nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of()
        ).join();
        var retrievedValue = UserData.objects.get(userId, "test_key");
        Assertions.assertTrue(retrievedValue.isEmpty());
    }
}
