package club.mondaylunch.gatos.basicnodes.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class BooleanOperationNodeTest {
    private static final Node OR        = Node.create(BasicNodes.BOOL_OP).modifySetting("config", DataType.STRING.create("or"));
    private static final Node AND       = Node.create(BasicNodes.BOOL_OP).modifySetting("config", DataType.STRING.create("and"));
    private static final Node XOR       = Node.create(BasicNodes.BOOL_OP).modifySetting("config", DataType.STRING.create("xor"));
    private static final Node NOT       = Node.create(BasicNodes.BOOL_OP).modifySetting("config", DataType.STRING.create("not"));

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.BOOL_OP);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void correctlyResolvesNegation() {
        Map<String, DataBox<?>> inputs = Map.of(
            "input", DataType.BOOLEAN.create(true)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, NOT.settings()).get("result").join().value();
        assertFalse(result);

        inputs = Map.of(
            "input", DataType.BOOLEAN.create(false)
        );
        result = (boolean) BasicNodes.BOOL_OP.compute(inputs, NOT.settings()).get("result").join().value();
        assertTrue(result);
    }

    @Test
    public void correctlyResolvesOrDiff() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(false)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, OR.settings()).get("result").join().value();
        assertTrue(result);
    }

    @Test
    public void correctlyResolvesOrTrue() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(true)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, OR.settings()).get("result").join().value();
        assertTrue(result);
    }

    @Test
    public void correctlyResolvesOrFalse() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(false),
                "inputB", DataType.BOOLEAN.create(false)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, OR.settings()).get("result").join().value();
        assertFalse(result);
    }

    @Test
    public void correctlyResolvesAndDiff() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(false)
        );

        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, AND.settings()).get("result").join().value();
        assertFalse(result);
    }

    @Test
    public void correctlyResolvesAndTrue() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(true)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, AND.settings()).get("result").join().value();
        assertTrue(result);
    }

    @Test
    public void correctlyResolvesAndFalse() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(false),
                "inputB", DataType.BOOLEAN.create(false)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, AND.settings()).get("result").join().value();
        assertFalse(result);
    }

    @Test
    public void correctlyResolvesXorDiff() {        
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(false)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, XOR.settings()).get("result").join().value();
        assertTrue(result);
    }

    @Test
    public void correctlyResolvesXorTrue() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(true),
                "inputB", DataType.BOOLEAN.create(true)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, XOR.settings()).get("result").join().value();
        assertFalse(result);
    }

    @Test
    public void correctlyResolvesXorFalse() {
        Map<String, DataBox<?>> inputs = Map.of(
                "inputA", DataType.BOOLEAN.create(false),
                "inputB", DataType.BOOLEAN.create(false)
        );
        boolean result = (boolean) BasicNodes.BOOL_OP.compute(inputs, XOR.settings()).get("result").join().value();
        assertFalse(result);
    }
}
