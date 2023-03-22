package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringRegexSplitNodeTest {
    private static final String TEST_STR = "I, EvaX humbly submit a toast to Nicholas Alexander";
    private static final String RGX = "[ ,.]";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_REGEX_SPLIT);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_REGEX_SPLIT);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyDoesDefaultRegexSplit() {
        var node = Node.create(BasicNodes.STRING_REGEX_SPLIT);
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var outputLst = (List<String>) BasicNodes.STRING_REGEX_SPLIT.compute(input, node.settings(), Map.of()).get("output").join().value();
        Assertions.assertEquals(Arrays.stream(TEST_STR.split("")).toList(), outputLst);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyDoesActualRegexSplit() {
        var node = Node.create(BasicNodes.STRING_REGEX_SPLIT)
            .modifySetting("regex", DataType.STRING.create(RGX));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var outputLst = (List<String>) BasicNodes.STRING_REGEX_SPLIT.compute(input, node.settings(), Map.of()).get("output").join().value();
        Assertions.assertEquals(Arrays.stream(TEST_STR.split(RGX)).toList(), outputLst);
    }
}
