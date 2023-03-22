package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringLengthNodeTest {
    private static final String TEST_STR = "The quick brown fox jumps over the lazy dog.";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_LENGTH);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_LENGTH);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesLength() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var output = BasicNodes.STRING_LENGTH.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(44.0, output.get("output").join().value());
    }

    @Test
    public void correctlyBehavesForIncorrectInputs() {
        Map<String, DataBox<?>> input0 = Map.of();
        var output0 = BasicNodes.STRING_LENGTH.compute(UUID.randomUUID(), input0, Map.of(), Map.of());
        Assertions.assertEquals(0.0, output0.get("output").join().value());
    }
}
