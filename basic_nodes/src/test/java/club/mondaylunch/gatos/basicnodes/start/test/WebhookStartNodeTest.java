package club.mondaylunch.gatos.basicnodes.start.test;

import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.WebhookStartNodeInput;
import club.mondaylunch.gatos.core.models.JsonObjectReference;

public class WebhookStartNodeTest {

    @Test
    public void areSettingsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_START);
        Assertions.assertTrue(node.settings().isEmpty());
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_START);
        Assertions.assertTrue(node.inputs().isEmpty());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.WEBHOOK_START);
        var outputs = node.outputs();
        Assertions.assertEquals(2, outputs.size());
        var nodeId = node.id();
        var requestBody = outputs.get("requestBody");
        Assertions.assertEquals(nodeId, requestBody.nodeId());
        Assertions.assertEquals("requestBody", requestBody.name());
        Assertions.assertEquals(DataType.JSON_OBJECT, requestBody.type());
        var endOutputReference = outputs.get("endOutputReference");
        Assertions.assertEquals(nodeId, endOutputReference.nodeId());
        Assertions.assertEquals("endOutputReference", endOutputReference.name());
        Assertions.assertEquals(DataType.REFERENCE, endOutputReference.type());
    }

    @Test
    public void computesCorrectly() {
        var json = new JsonObject();
        json.addProperty("test", 1);
        var reference = new JsonObjectReference();
        var input = new WebhookStartNodeInput(json, reference);
        var result = BasicNodes.WEBHOOK_START.compute(UUID.randomUUID(), input, Map.of());
        var requestBody = result.get("requestBody");
        Assertions.assertEquals(json, requestBody.join().value());
        var endOutputReference = result.get("endOutputReference");
        Assertions.assertEquals(reference, endOutputReference.join().value());
    }
}
