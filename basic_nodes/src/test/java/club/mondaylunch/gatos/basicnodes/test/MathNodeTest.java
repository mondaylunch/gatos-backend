package club.mondaylunch.gatos.basicnodes.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class MathNodeTest {
    private static final Node ADD = Node.create(BasicNodes.MATH).modifySetting("config", DataType.STRING.create("+"));
    private static final Node SUB = Node.create(BasicNodes.MATH).modifySetting("config", DataType.STRING.create("-"));
    private static final Node MLT = Node.create(BasicNodes.MATH).modifySetting("config", DataType.STRING.create("*"));
    private static final Node DIV = Node.create(BasicNodes.MATH).modifySetting("config", DataType.STRING.create("/"));

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.MATH);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void integerAddition() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(15)
        );
        int result = (int) BasicNodes.MATH.compute(inputs, ADD.settings()).get("output").join().value();
        assertEquals(25, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(-15)
        );
        result = (int) BasicNodes.MATH.compute(inputs, ADD.settings()).get("output").join().value();
        assertEquals(-5, result);
    }

    @Test
    public void integerSubtraction() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(15)
        );
        int result = (int) BasicNodes.MATH.compute(inputs, SUB.settings()).get("output").join().value();
        assertEquals(-5, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(-15)
        );
        result = (int) BasicNodes.MATH.compute(inputs, SUB.settings()).get("output").join().value();
        assertEquals(25, result);
    }

    @Test
    public void integerMultiplication() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(15)
        );
        int result = (int) BasicNodes.MATH.compute(inputs, MLT.settings()).get("output").join().value();
        assertEquals(150, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(-15)
        );
        result = (int) BasicNodes.MATH.compute(inputs, MLT.settings()).get("output").join().value();
        assertEquals(-150, result);
    }

    @Test
    public void integerDivision() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.INTEGER.create(90),
                "inputB", DataType.INTEGER.create(15)
        );
        int result = (int) BasicNodes.MATH.compute(inputs, DIV.settings()).get("output").join().value();
        assertEquals(6, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(-10),
                "inputB", DataType.INTEGER.create(2)
        );
        result = (int) BasicNodes.MATH.compute(inputs, DIV.settings()).get("output").join().value();
        assertEquals(-5, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(10),
                "inputB", DataType.INTEGER.create(15)
        );
        result = (int) BasicNodes.MATH.compute(inputs, DIV.settings()).get("output").join().value();
        assertEquals(0, result);

        inputs = Map.of(
                "inputA", DataType.INTEGER.create(15),
                "inputB", DataType.INTEGER.create(10)
        );
        result = (int) BasicNodes.MATH.compute(inputs, DIV.settings()).get("output").join().value();
        assertEquals(1, result);

    }
}
