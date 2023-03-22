package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringConcatNodeTest {
    private static final List<String> TEST_LIST = List.of("Subject", "Verb", "Object");

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CONCAT);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CONCAT);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyCombinesStrings() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.listOf().create(TEST_LIST)
        );
        var output = BasicNodes.STRING_CONCAT.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(output.get("output").join().value(), String.join("", TEST_LIST));
    }

    @Test
    public void correctlyCombinesWithCustomSettings() {
        var node = Node.create(BasicNodes.STRING_CONCAT)
            .modifySetting("delimiter", DataType.STRING.create(" amongus "));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.listOf().create(TEST_LIST)
        );
        var output = BasicNodes.STRING_CONCAT.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(output.get("output").join().value(), String.join(" amongus ", TEST_LIST));
    }
}
