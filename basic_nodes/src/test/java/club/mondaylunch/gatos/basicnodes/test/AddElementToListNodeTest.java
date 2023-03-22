package club.mondaylunch.gatos.basicnodes.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class AddElementToListNodeTest {
    private static final List<Double> TEST_NUM_LIST = List.of(1.0, 2.0, 3.0);
    private static final List<Boolean> TEST_EMPTY_LIST = new LinkedList<>();

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.ADD_ELEM_TO_LIST);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.ADD_ELEM_TO_LIST);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("list"));
        Assertions.assertTrue(node.inputs().containsKey("element"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.ADD_ELEM_TO_LIST);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyAddsToEmptyList() {
        var node = Node.create(BasicNodes.ADD_ELEM_TO_LIST);
        Map<String, DataBox<?>> inputs = Map.of(
            "list", DataType.BOOLEAN.listOf().create(TEST_EMPTY_LIST),
            "element", DataType.BOOLEAN.create(true)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "list", DataType.BOOLEAN.listOf(),
            "element", DataType.BOOLEAN
        );
        var output = BasicNodes.ADD_ELEM_TO_LIST.compute(inputs, node.settings(), inputTypes);
        Assertions.assertEquals(List.of(true), output.get("output").join().value());
    }

    @Test
    public void correctlyAddsToExistingList() {
        var node = Node.create(BasicNodes.ADD_ELEM_TO_LIST);
        Map<String, DataBox<?>> inputs = Map.of(
            "list", DataType.NUMBER.listOf().create(TEST_NUM_LIST),
            "element", DataType.NUMBER.create((double) 4)
        );
        Map<String, DataType<?>> inputTypes = Map.of(
            "list", DataType.NUMBER.listOf(),
            "element", DataType.NUMBER
        );
        var output = BasicNodes.ADD_ELEM_TO_LIST.compute(inputs, node.settings(), inputTypes);
        Assertions.assertEquals(List.of(1.0, 2.0, 3.0, 4.0), output.get("output").join().value());

        inputs = Map.of(
            "list", DataType.BOOLEAN.listOf().create(List.of(true, false, true)),
            "element", DataType.BOOLEAN.create(false)
        );
        inputTypes = Map.of(
            "list", DataType.BOOLEAN.listOf(),
            "element", DataType.BOOLEAN
        );
        output = BasicNodes.ADD_ELEM_TO_LIST.compute(inputs, node.settings(), inputTypes);
        Assertions.assertEquals(List.of(true, false, true, false), output.get("output").join().value());   
    }

    @Test
    public void correctlyIgnoresIncompatibleTypes() {
        var node = Node.create(BasicNodes.ADD_ELEM_TO_LIST);
        Map<String, DataBox<?>> inputs = Map.of(
            "list", DataType.NUMBER.listOf().create(TEST_NUM_LIST),
            "element", DataType.STRING.create("true")
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "list", DataType.NUMBER.listOf(),
            "element", DataType.STRING
        );
        Assertions.assertThrows(NoSuchElementException.class, () -> BasicNodes.ADD_ELEM_TO_LIST.compute(inputs, node.settings(), inputTypes));
    }
}
