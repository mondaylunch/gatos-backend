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

public class IncrementFlowDataNodeTest {

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
        var settings = BasicNodes.INCREMENT_FLOW_DATA.settings();
        Assertions.assertEquals(2, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
        Assertions.assertTrue(settings.containsKey("set_if_absent"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.INCREMENT_FLOW_DATA);
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
        var node = Node.create(BasicNodes.INCREMENT_FLOW_DATA)
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
        var node = Node.create(BasicNodes.INCREMENT_FLOW_DATA);
        Assertions.assertTrue(node.outputs().isEmpty());
    }

    @Test
    public void canIncrementData() {
        var nodeType = BasicNodes.INCREMENT_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.NUMBER.create(1.0);
        FlowData.objects.set(flowId, "test_key", value);
        nodeType.compute(
            flowId,
            Map.of("key", key, "value", value),
            Map.of("set_if_absent", DataType.BOOLEAN.create(false))
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrievedValue.orElseThrow());
    }

    @Test
    public void canIncrementOrSetExistentData() {
        var nodeType = BasicNodes.INCREMENT_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.NUMBER.create(1.0);
        FlowData.objects.set(flowId, "test_key", value);
        nodeType.compute(
            flowId,
            Map.of("key", key, "value", value),
            Map.of()
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(DataType.NUMBER.create(2.0), retrievedValue.orElseThrow());
    }

    @Test
    public void canIncrementOrSetNonExistentData() {
        var nodeType = BasicNodes.INCREMENT_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var key = DataType.STRING.create("test_key");
        var value = DataType.NUMBER.create(1.0);
        nodeType.compute(
            flowId,
            Map.of("key", key, "value", value),
            Map.of()
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isPresent());
        Assertions.assertEquals(value, retrievedValue.orElseThrow());
    }
}
