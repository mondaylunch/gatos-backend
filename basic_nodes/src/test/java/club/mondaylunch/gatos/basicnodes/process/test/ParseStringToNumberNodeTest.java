package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class ParseStringToNumberNodeTest {

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.PARSE_STRING_TO_NUMBER);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING_TO_NUMBER);
        Assertions.assertEquals(node.inputs().size(), 1);
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING_TO_NUMBER);
        Assertions.assertEquals(node.outputs().size(), 1);
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyParsesInt() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("1")
        );
        double output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(1, output);

        input = Map.of(
            "input", DataType.STRING.create("11")
        );
        output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(11, output);
    }

    @Test
    public void correctlyParsesCommaSeperatedInt() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("11,111")
        );
        double output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(11111, output);
    }

    @Test
    public void correctlyParsesDouble() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("11.11")
        );
        double output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(11.11, output);
    }

    @Test
    public void correctlyParsesCommaSeperatedDouble() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("11,111.11")
        );
        double output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(11111.11, output);

        input = Map.of(
            "input", DataType.STRING.create("11,111,111.11")
        );
        output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(11111111.11, output);
    }

    @Test
    void correctlyNaNsInvalid() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create("11.11.11")
        );
        double output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(Double.NaN, output);

        input = Map.of(
            "input", DataType.STRING.create("hello I am an integer")    // the jester doth lie most fiendishly
        );
        output = (double) BasicNodes.PARSE_STRING_TO_NUMBER.compute(input).get("output").join().value();
        Assertions.assertEquals(Double.NaN, output);
    }
}
