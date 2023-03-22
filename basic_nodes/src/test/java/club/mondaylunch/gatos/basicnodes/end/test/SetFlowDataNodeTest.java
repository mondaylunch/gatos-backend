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
import club.mondaylunch.gatos.core.models.FlowData;

public class SetFlowDataNodeTest {

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
    public void areSettingsCorrect() {
        var settings = BasicNodes.SET_FLOW_DATA.settings();
        Assertions.assertEquals(2, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
        Assertions.assertTrue(settings.containsKey("overwrite"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.SET_FLOW_DATA);
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
        Assertions.assertEquals(DataType.ANY, valueInput.type());
    }

    @Test
    public void areInputsCorrectWithKeySetting() {
        var node = Node.create(BasicNodes.SET_FLOW_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(1, inputs.size());
        var nodeId = node.id();
        var valueInput = inputs.get("value");
        Assertions.assertEquals(nodeId, valueInput.nodeId());
        Assertions.assertEquals("value", valueInput.name());
        Assertions.assertEquals(DataType.ANY, valueInput.type());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.SET_FLOW_DATA);
        Assertions.assertTrue(node.getOutputs().isEmpty());
    }

    @Test
    public void canSetDataWithSettingsKey() {
        var nodeType = BasicNodes.SET_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.STRING.create("Test value");
        nodeType.compute(
            flowId,
            Map.of("value", value),
            Map.of("key", key)
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(value, retrievedValue.orElseThrow());
    }

    @Test
    public void canSetDataWithInputKey() {
        var nodeType = BasicNodes.SET_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.STRING.create("Test value");
        nodeType.compute(
            flowId,
            Map.of("value", value, "key", key),
            Map.of()
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(value, retrievedValue.orElseThrow());
    }

    @Test
    public void canSetDataWithSettingsAndInputsKey() {
        var nodeType = BasicNodes.SET_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var settingsKey = DataType.STRING.create("settings_key");
        var inputKey = DataType.STRING.create("input_key");
        var value = DataType.STRING.create("Test value");
        nodeType.compute(
            flowId,
            Map.of("key", inputKey, "value", value),
            Map.of("key", settingsKey)
        ).join();
        var settingsKeyRetrievedValue = FlowData.objects.get(flowId, "settings_key");
        Assertions.assertTrue(settingsKeyRetrievedValue.isPresent());
        Assertions.assertEquals(value, settingsKeyRetrievedValue.orElseThrow());
        var inputKeyRetrievedValue = FlowData.objects.get(flowId, "input_key");
        Assertions.assertTrue(inputKeyRetrievedValue.isEmpty());
    }

    @Test
    public void canSetDataIfAbsentWithAbsent() {
        var nodeType = BasicNodes.SET_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.STRING.create("Test value");
        nodeType.compute(
            flowId,
            Map.of("value", value, "key", key),
            Map.of("overwrite", DataType.BOOLEAN.create(false))
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(value, retrievedValue.orElseThrow());
    }

    @Test
    public void canSetDataIfAbsentWithPresent() {
        var nodeType = BasicNodes.SET_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.STRING.create("Test value");
        var otherValue = DataType.STRING.create("Other value");
        FlowData.objects.set(flowId, "test_key", value);
        nodeType.compute(
            flowId,
            Map.of("value", otherValue, "key", key),
            Map.of("overwrite", DataType.BOOLEAN.create(false))
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(value, retrievedValue.orElseThrow());
    }
}
