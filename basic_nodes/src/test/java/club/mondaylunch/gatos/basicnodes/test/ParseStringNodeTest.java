package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ParseStringNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING);
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
    public void correctlyParsesInt() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("1")
        );
        var output = BasicNodes.STRING_LENGTH.compute(input, Map.of());
        Assertions.assertEquals(output.get("output").join().value(), 1);
    }
}
