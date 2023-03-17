package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.graph.Node;

public class CreateJSONNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.CREATE_JSON);
        Assertions.assertEquals(0, node.inputs().size());
        Assertions.assertFalse(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.CREATE_JSON);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void identifiesEqualStrings() {
        var output = BasicNodes.CREATE_JSON.compute(Map.of(), Map.of(), Map.of());
        Assertions.assertEquals(new JsonObject(), output.get("output").join().value());
    }
}
