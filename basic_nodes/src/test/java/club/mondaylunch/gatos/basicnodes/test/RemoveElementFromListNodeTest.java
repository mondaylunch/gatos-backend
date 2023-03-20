package club.mondaylunch.gatos.basicnodes.test;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.RemoveElementFromListNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class RemoveElementFromListNodeTest {
    private static final Node TEST_ELEMENT_REMOVE = Node.create(BasicNodes.REMOVE_ELEM_FROM_LIST).modifySetting("mode", RemoveElementFromListNodeType.ELEMENT_REFERENCE.create(RemoveElementFromListNodeType.Mode.ELEMENT));
    private static final Node TEST_INDEX_REMOVE = Node.create(BasicNodes.REMOVE_ELEM_FROM_LIST).modifySetting("mode", RemoveElementFromListNodeType.ELEMENT_REFERENCE.create(RemoveElementFromListNodeType.Mode.INDEX));

    private static final List<Number> TEST_NUM_LIST = List.of(1, 2, 3);

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.REMOVE_ELEM_FROM_LIST);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = TEST_ELEMENT_REMOVE;
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("list"));
        Assertions.assertTrue(node.inputs().containsKey("element"));

        node = TEST_INDEX_REMOVE;
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("list"));
        Assertions.assertTrue(node.inputs().containsKey("index"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.REMOVE_ELEM_FROM_LIST);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyRemovesFromExistingList() {
        var node = TEST_ELEMENT_REMOVE;
        Map<String, DataBox<?>> inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "element", DataType.ANY.create(1)
        );
        var output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(2, 3), output.get("output").join().value());

        inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "element", DataType.ANY.create(2)
        );
        output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(1, 3), output.get("output").join().value());

        inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "element", DataType.ANY.create(3)
        );
        output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(1, 2), output.get("output").join().value());

        node = TEST_INDEX_REMOVE;
        inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "index", DataType.NUMBER.create(0.0)
        );
        output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(2, 3), output.get("output").join().value());

        inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "index", DataType.NUMBER.create(1.0)
        );
        output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(1, 3), output.get("output").join().value());

        inputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "index", DataType.NUMBER.create(2.0)
        );
        output = BasicNodes.REMOVE_ELEM_FROM_LIST.compute(inputs, node.settings(), Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(List.of(1, 2), output.get("output").join().value());
    }
}
