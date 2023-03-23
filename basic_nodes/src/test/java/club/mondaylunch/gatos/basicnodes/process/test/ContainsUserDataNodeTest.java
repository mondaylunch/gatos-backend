package club.mondaylunch.gatos.basicnodes.process.test;

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

public class ContainsUserDataNodeTest {

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
        var settings = BasicNodes.CONTAINS_USER_DATA.settings();
        Assertions.assertEquals(2, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
        Assertions.assertTrue(settings.containsKey("type"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.CONTAINS_USER_DATA);
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
        var node = Node.create(BasicNodes.CONTAINS_USER_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(0, inputs.size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.CONTAINS_USER_DATA);
        var outputs = node.outputs();
        Assertions.assertEquals(1, outputs.size());
        var nodeId = node.id();
        var output = outputs.get("contains");
        Assertions.assertNotNull(output);
        Assertions.assertEquals(nodeId, output.nodeId());
        Assertions.assertEquals("contains", output.name());
        Assertions.assertEquals(DataType.BOOLEAN, output.type());
    }

    @Test
    public void doesContain() {
        var nodeType = BasicNodes.CONTAINS_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of(),
            Map.of()
        );
        var containsFuture = outputs.get("contains");
        Assertions.assertNotNull(containsFuture);
        var expectedContains = DataType.BOOLEAN.create(true);
        var retrievedContains = containsFuture.join();
        Assertions.assertEquals(expectedContains, retrievedContains);
    }

    @Test
    public void doesContainWithCorrectType() {
        var nodeType = BasicNodes.CONTAINS_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of("type", DataType.DATA_TYPE.create(DataType.STRING)),
            Map.of()
        );
        var containsFuture = outputs.get("contains");
        Assertions.assertNotNull(containsFuture);
        var expectedContains = DataType.BOOLEAN.create(true);
        var retrievedContains = containsFuture.join();
        Assertions.assertEquals(expectedContains, retrievedContains);
    }

    @Test
    public void doesNotContainWithIncorrectType() {
        var nodeType = BasicNodes.CONTAINS_USER_DATA;
        var userId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        UserData.objects.set(userId, "test_key", value);
        var outputs = nodeType.compute(
            userId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of("type", DataType.DATA_TYPE.create(DataType.NUMBER)),
            Map.of()
        );
        var containsFuture = outputs.get("contains");
        Assertions.assertNotNull(containsFuture);
        var expectedContains = DataType.BOOLEAN.create(false);
        var retrievedContains = containsFuture.join();
        Assertions.assertEquals(expectedContains, retrievedContains);
    }
}
