package club.mondaylunch.gatos.basicnodes.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.MathNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class MathNodeTest {
    private static final Node ADD = Node.create(BasicNodes.MATH).modifySetting("operator", MathNodeType.MATHEMATICAL_OPERATOR.create(MathNodeType.Operator.ADDITION));
    private static final Node SUB = Node.create(BasicNodes.MATH).modifySetting("operator", MathNodeType.MATHEMATICAL_OPERATOR.create(MathNodeType.Operator.SUBTRACTION));
    private static final Node MLT = Node.create(BasicNodes.MATH).modifySetting("operator", MathNodeType.MATHEMATICAL_OPERATOR.create(MathNodeType.Operator.MULTIPLICATION));
    private static final Node DIV = Node.create(BasicNodes.MATH).modifySetting("operator", MathNodeType.MATHEMATICAL_OPERATOR.create(MathNodeType.Operator.DIVISION));

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.MATH);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.MATH);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("inputA"));
        Assertions.assertTrue(node.inputs().containsKey("inputB"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.MATH);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void integerAddition() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, ADD.settings(), Map.of()).get("output").join().value();
        assertEquals(25, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) -15)
        );
        result = (double) BasicNodes.MATH.compute(inputs, ADD.settings(), Map.of()).get("output").join().value();
        assertEquals(-5, result);
    }

    @Test
    public void integerSubtraction() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, SUB.settings(), Map.of()).get("output").join().value();
        assertEquals(-5, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) -15)
        );
        result = (double) BasicNodes.MATH.compute(inputs, SUB.settings(), Map.of()).get("output").join().value();
        assertEquals(25, result);
    }

    @Test
    public void integerMultiplication() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, MLT.settings(), Map.of()).get("output").join().value();
        assertEquals(150, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) -15)
        );
        result = (double) BasicNodes.MATH.compute(inputs, MLT.settings(), Map.of()).get("output").join().value();
        assertEquals(-150, result);
    }

    @Test
    public void integerDivision() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create((double) 90),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(6, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) -10),
            "inputB", DataType.NUMBER.create((double) 2)
        );
        result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(-5, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) 20)
        );
        result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(0.5, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 10),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals((double) 2 / 3, result);

        inputs = Map.of("inputA", DataType.NUMBER.create((double) 15),
            "inputB", DataType.NUMBER.create((double) 10)
        );
        result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(1.5, result);
    }

    @Test
    public void doubleAddition() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create( 10.25),
            "inputB", DataType.NUMBER.create( 9.75)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, ADD.settings(), Map.of()).get("output").join().value();
        assertEquals(20, result);

        inputs = Map.of("inputA", DataType.NUMBER.create( 9.75),
            "inputB", DataType.NUMBER.create( -8.25)
        );
        result = (double) BasicNodes.MATH.compute(inputs, ADD.settings(), Map.of()).get("output").join().value();
        assertEquals(1.5, result);
    }

    @Test
    public void doubleSubtraction() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create(11.45),
            "inputB", DataType.NUMBER.create(0.25)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, SUB.settings(), Map.of()).get("output").join().value();
        assertEquals(11.2, result);

        inputs = Map.of("inputA", DataType.NUMBER.create(100.001),
            "inputB", DataType.NUMBER.create(-99.999)
        );
        result = (double) BasicNodes.MATH.compute(inputs, SUB.settings(), Map.of()).get("output").join().value();
        assertEquals(200, result);
    }

    @Test
    public void doubleMultiplication() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create( 0.1),
            "inputB", DataType.NUMBER.create((double) 15)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, MLT.settings(), Map.of()).get("output").join().value();
        assertEquals(1.5, result);

        inputs = Map.of("inputA", DataType.NUMBER.create( 0.25),
            "inputB", DataType.NUMBER.create( 3.1)
        );
        result = (double) BasicNodes.MATH.compute(inputs, MLT.settings(), Map.of()).get("output").join().value();
        assertEquals(0.775, result);
    }

    @Test
    public void doubleDivision() {
        Map<String, DataBox<?>> inputs = Map.of("inputA", DataType.NUMBER.create( 1.5),
            "inputB", DataType.NUMBER.create( 0.75)
        );
        double result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(2, result);

        inputs = Map.of("inputA", DataType.NUMBER.create( 74.213),
            "inputB", DataType.NUMBER.create( 2.4)
        );
        result = (double) BasicNodes.MATH.compute(inputs, DIV.settings(), Map.of()).get("output").join().value();
        assertEquals(30.9220833333333333, result);
    }
}
