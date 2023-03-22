package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class WebhookEndNodeTest {

    @Test
    public void areSettingsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_END);
        Assertions.assertTrue(node.settings().isEmpty());
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_END);
        var inputs = node.inputs();
        Assertions.assertEquals(2, inputs.size());
        var nodeId = node.id();
        var graphOutput = inputs.get("graphOutput");
        Assertions.assertEquals(nodeId, graphOutput.nodeId());
        Assertions.assertEquals("graphOutput", graphOutput.name());
        Assertions.assertEquals(DataType.JSON_OBJECT, graphOutput.type());
        var outputReference = inputs.get("outputReference");
        Assertions.assertEquals(nodeId, outputReference.nodeId());
        Assertions.assertEquals("outputReference", outputReference.name());
        Assertions.assertEquals(DataType.REFERENCE, outputReference.type());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_END);
        Assertions.assertTrue(node.getOutputs().isEmpty());
    }

    @Test
    public void computesCorrectly() {
        var graphOutput = new JsonObject();
        graphOutput.addProperty("test", 1);
        AtomicReference<?> outputReference = new AtomicReference<>();
        Map<String, DataBox<?>> inputs = Map.of(
            "graphOutput", DataType.JSON_OBJECT.create(graphOutput),
            "outputReference", DataType.REFERENCE.create(outputReference)
        );
        BasicNodes.WEBHOOK_END.compute(UUID.randomUUID(), inputs, Map.of());
        Assertions.assertEquals(graphOutput, outputReference.get());
    }
}
