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

public class RemoveFlowDataNodeTest {

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
        var settings = BasicNodes.REMOVE_FLOW_DATA.settings();
        Assertions.assertEquals(1, settings.size());
        Assertions.assertTrue(settings.containsKey("key"));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.REMOVE_FLOW_DATA);
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
        var node = Node.create(BasicNodes.REMOVE_FLOW_DATA)
            .modifySetting("key", DataType.STRING.create("test_key"));
        var inputs = node.inputs();
        Assertions.assertEquals(0, inputs.size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.REMOVE_FLOW_DATA);
        Assertions.assertTrue(node.outputs().isEmpty());
    }

    @Test
    public void canRemoveData() {
        var nodeType = BasicNodes.REMOVE_FLOW_DATA;
        var flowId = UUID.randomUUID();
        var value = DataType.STRING.create("Test value");
        FlowData.objects.set(flowId, "test_key", value);
        nodeType.compute(
            flowId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of()
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isEmpty());
    }

    @Test
    public void canRemoveNonExistentData() {
        var nodeType = BasicNodes.REMOVE_FLOW_DATA;
        var flowId = UUID.randomUUID();
        nodeType.compute(
            flowId,
            Map.of("key", DataType.STRING.create("test_key")),
            Map.of()
        ).join();
        var retrievedValue = FlowData.objects.get(flowId, "test_key");
        Assertions.assertTrue(retrievedValue.isEmpty());
    }
}
