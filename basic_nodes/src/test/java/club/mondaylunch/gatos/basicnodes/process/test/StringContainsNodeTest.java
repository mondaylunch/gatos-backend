package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringContainsNodeTest {
    private static final String TEST_STR = "Lorem Ipsum Dolor Sit Amet";
    private static final String TEST_SUBSTR = "Dolor";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CONTAINS);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CONTAINS);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesIfSubstrPresent() {
        var node = Node.create(BasicNodes.STRING_CONTAINS)
            .modifySetting("substring", DataType.STRING.create(TEST_SUBSTR));
        Map<String, DataBox<?>> input0 = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var output0 = BasicNodes.STRING_CONTAINS.compute(UUID.randomUUID(), input0, node.settings(), Map.of());
        Assertions.assertTrue((boolean) output0.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesIfSubstrAbsent() {
        var node = Node.create(BasicNodes.STRING_CONTAINS)
            .modifySetting("substring", DataType.STRING.create(TEST_SUBSTR));
        Map<String, DataBox<?>> input1 = Map.of(
            "input", DataType.STRING.create(TEST_STR.toUpperCase())
        );
        var output1 = BasicNodes.STRING_CONTAINS.compute(UUID.randomUUID(), input1, node.settings(), Map.of());
        Assertions.assertFalse((boolean) output1.get("output").join().value());
    }
}
