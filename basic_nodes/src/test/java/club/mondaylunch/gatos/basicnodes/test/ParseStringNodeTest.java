package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class ParseStringNodeTest {

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.PARSE_STRING);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING);
        Assertions.assertEquals(node.inputs().size(), 1);
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING);
        Assertions.assertEquals(node.getOutputs().size(), 1);
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyParsesInt() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("1")
        );
        int output = (int) BasicNodes.PARSE_STRING.compute(input, Map.of()).get("output").join().value();
        Assertions.assertEquals(1, output);
        
        input = Map.of(
            "input", DataType.STRING.create("11")
        );
        output = (int) BasicNodes.PARSE_STRING.compute(input, Map.of()).get("output").join().value();
        Assertions.assertEquals(11, output);
    }
}