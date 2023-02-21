package club.mondaylunch.gatos.basicnodes.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.NumberOrderingNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class NumberOrderingNodeTest {
    private static final DataBox<Double> LOW = DataType.NUMBER.create( (double) -9999999);
    private static final DataBox<Double> DCL = DataType.NUMBER.create( (double) -0.9999999);
    private static final DataBox<Double> MED = DataType.NUMBER.create( (double) 0);
    private static final DataBox<Double> DCH = DataType.NUMBER.create( (double) 0.9999999);
    private static final DataBox<Double> HIGH = DataType.NUMBER.create( (double) 9999999);

    private static final Node GT    = Node.create(BasicNodes.NUMBER_ORDERING).modifySetting("mode", NumberOrderingNodeType.NUMBER_ORDERING_MODE.create(NumberOrderingNodeType.Mode.GREATERTHAN));
    private static final Node LT    = Node.create(BasicNodes.NUMBER_ORDERING).modifySetting("mode", NumberOrderingNodeType.NUMBER_ORDERING_MODE.create(NumberOrderingNodeType.Mode.LESSTHAN));
    private static final Node GTEQ  = Node.create(BasicNodes.NUMBER_ORDERING).modifySetting("mode", NumberOrderingNodeType.NUMBER_ORDERING_MODE.create(NumberOrderingNodeType.Mode.GREATERTHANEQ));
    private static final Node LTEQ  = Node.create(BasicNodes.NUMBER_ORDERING).modifySetting("mode", NumberOrderingNodeType.NUMBER_ORDERING_MODE.create(NumberOrderingNodeType.Mode.LESSTHANEQ));

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.NUMBER_ORDERING);
        Assertions.assertTrue(graph.containsNode(node));
    }
    
    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.NUMBER_ORDERING);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("inputA"));
        Assertions.assertTrue(node.inputs().containsKey("inputB"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.NUMBER_ORDERING);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesGreaterThan() {
        Map<String, DataBox<?>> inputs = Map.of(
            "inputA", HIGH, 
            "inputB", LOW
        );
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GT.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", LOW,
            "inputB", HIGH
        );        
        assertFalse((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GT.settings()).get("output").join().value());
        
        inputs = Map.of(
            "inputA", LOW,
            "inputB", DCL
        );        
        assertFalse((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GT.settings()).get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLessThan() {
        Map<String, DataBox<?>> inputs = Map.of(
            "inputA", HIGH, 
            "inputB", LOW
        );
        assertFalse((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LT.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", LOW,
            "inputB", HIGH
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LT.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", DCL,
            "inputB", HIGH
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LT.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", MED,
            "inputB", DCH
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LT.settings()).get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesGreaterThanOrEqualTo() {
        Map<String, DataBox<?>> inputs = Map.of(
            "inputA", HIGH, 
            "inputB", LOW
        );
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", LOW,
            "inputB", HIGH
        );        
        assertFalse((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", MED,
            "inputB", MED
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", DCH,
            "inputB", DCH
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GTEQ.settings()).get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLessThanOrEqualTo() {
        Map<String, DataBox<?>> inputs = Map.of(
            "inputA", HIGH, 
            "inputB", LOW
        );
        assertFalse((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", LOW,
            "inputB", HIGH
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", MED,
            "inputB", MED
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, LTEQ.settings()).get("output").join().value());

        inputs = Map.of(
            "inputA", DCL,
            "inputB", DCL
        );        
        assertTrue((boolean) BasicNodes.NUMBER_ORDERING.compute(inputs, GTEQ.settings()).get("output").join().value());
    }
}
