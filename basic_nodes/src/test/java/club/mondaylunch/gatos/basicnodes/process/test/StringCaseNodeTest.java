package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.process.StringCaseNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class StringCaseNodeTest {
    private static final String TEST_STR = "A dog walked into a tavern and said, 'I can't see a thing. I'll open this one'";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CASE);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.STRING_CASE);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyDoesUpperCase() {
        var node = Node.create(BasicNodes.STRING_CASE)
            .modifySetting("case_setting", StringCaseNodeType.getCaseSettingOf("upper"));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var output = BasicNodes.STRING_CASE.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(TEST_STR.toUpperCase(), output.get("output").join().value());
    }

    @Test
    public void correctlyDoesLowerCase() {
        var node = Node.create(BasicNodes.STRING_CASE)
            .modifySetting("case_setting", StringCaseNodeType.getCaseSettingOf("lower"));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(TEST_STR)
        );
        var output = BasicNodes.STRING_CASE.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(TEST_STR.toLowerCase(), output.get("output").join().value());
    }
}
