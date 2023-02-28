package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IsFiniteNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.IS_FINITE);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.IS_FINITE);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesFiniteInput() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.NUMBER.create(5.0)
        );
        var output = BasicNodes.IS_FINITE.compute(input, Map.of(), Map.of());
        Assertions.assertTrue((boolean) output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesPositiveInfinity() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.NUMBER.create(Double.POSITIVE_INFINITY)
        );
        var output = BasicNodes.IS_FINITE.compute(input, Map.of(), Map.of());
        Assertions.assertFalse((boolean) output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesNegativeInfinity() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.NUMBER.create(Double.NEGATIVE_INFINITY)
        );
        var output = BasicNodes.IS_FINITE.compute(input, Map.of(), Map.of());
        Assertions.assertFalse((boolean) output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesNaN() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.NUMBER.create(Double.NaN)
        );
        var output = BasicNodes.IS_FINITE.compute(input, Map.of(), Map.of());
        Assertions.assertFalse((boolean) output.get("output").join().value());
    }
}
