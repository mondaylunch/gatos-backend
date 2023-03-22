package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringRegexReplacementNodeTest {
    private static final String TEST_STR = "Box Fox Ox Pox Mailbox";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_REGEX_REPLACE);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_REGEX_REPLACE);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyDoesRegexReplacement() {
        var oldRgx = "ox";
        var newRgx = "oxen";
        var node = Node.create(BasicNodes.STRING_REGEX_REPLACE)
            .modifySetting("regex_old", DataType.STRING.create(oldRgx))
            .modifySetting("regex_new", DataType.STRING.create(newRgx));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var output = BasicNodes.STRING_REGEX_REPLACE.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(TEST_STR.replace(oldRgx, newRgx), output.get("output").join().value());
    }
}
