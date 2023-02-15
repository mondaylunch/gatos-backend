package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListLengthNodeTest {
    private static final List<String> TEST_EMPTY_ARRAY_LIST = new ArrayList<>();
    private static final List<String> TEST_EMPTY_LINKED_LIST = new LinkedList<>();

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_LENGTH);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_LENGTH);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesEmptyArrayListsLength() {
        Map<String, DataBox<?>> inputWithArrayList = Map.of(
            "input", DataType.LIST.create(TEST_EMPTY_ARRAY_LIST)
        );
        var outputWithArrayList = BasicNodes.LIST_LENGTH.compute(inputWithArrayList, Map.of());
        Assertions.assertEquals(outputWithArrayList.get("output").join().value(), 0);
    }

    @Test
    public void correctlyEvaluatesEmptyLinkedListsLength() {
        Map<String, DataBox<?>> inputWithLinkedList = Map.of(
            "input", DataType.LIST.create(TEST_EMPTY_LINKED_LIST)
        );
        var outputWithLinkedList = BasicNodes.LIST_LENGTH.compute(inputWithLinkedList, Map.of());
        Assertions.assertEquals(outputWithLinkedList.get("output").join().value(), 0);
    }

    @Test
    public void correctlyEvaluatesNonEmptyArrayListsLength() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> inputWithArrayList = Map.of(
            "input", DataType.LIST.create(testArrayList)
        );
        var outputWithArrayList = BasicNodes.LIST_LENGTH.compute(inputWithArrayList, Map.of());
        Assertions.assertEquals(outputWithArrayList.get("output").join().value(), 10);
    }

    @Test
    public void correctlyEvaluatesNonEmptyLinkedListsLength() {
        List<String> testLinkedList = new LinkedList<>();
        Map<String, DataBox<?>> inputWithLinkedList = Map.of(
            "input", DataType.LIST.create(testLinkedList)
        );
        for (int i = 0; i < 10; i++) {
            testLinkedList.add("test");
        }
        var outputWithLinkedList = BasicNodes.LIST_LENGTH.compute(inputWithLinkedList, Map.of());
        Assertions.assertEquals(outputWithLinkedList.get("output").join().value(), 10);
    }

    @Test
    public void correctlyBehavesForIncorrectInputs() {
        Map<String, DataBox<?>> input0 = Map.of();
        var output0 = BasicNodes.LIST_LENGTH.compute(input0, Map.of());
        Assertions.assertEquals(output0.get("output").join().value(), 0);
    }
}
