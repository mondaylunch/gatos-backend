package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.ListDataType;
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
            "input", ListDataType.GENERIC_LIST.create(TEST_EMPTY_ARRAY_LIST)
        );
        var outputWithArrayList = BasicNodes.LIST_LENGTH.compute(UUID.randomUUID(), inputWithArrayList, Map.of(), Map.of());
        Assertions.assertEquals(0.0, outputWithArrayList.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesEmptyLinkedListsLength() {
        Map<String, DataBox<?>> inputWithLinkedList = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_EMPTY_LINKED_LIST)
        );
        var outputWithLinkedList = BasicNodes.LIST_LENGTH.compute(UUID.randomUUID(), inputWithLinkedList, Map.of(), Map.of());
        Assertions.assertEquals(0.0, outputWithLinkedList.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesNonEmptyArrayListsLength() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> inputWithArrayList = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList)
        );
        var outputWithArrayList = BasicNodes.LIST_LENGTH.compute(UUID.randomUUID(), inputWithArrayList, Map.of(), Map.of());
        Assertions.assertEquals(10.0, outputWithArrayList.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesNonEmptyLinkedListsLength() {
        List<String> testLinkedList = new LinkedList<>();
        Map<String, DataBox<?>> inputWithLinkedList = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testLinkedList)
        );
        for (int i = 0; i < 10; i++) {
            testLinkedList.add("test");
        }
        var outputWithLinkedList = BasicNodes.LIST_LENGTH.compute(UUID.randomUUID(), inputWithLinkedList, Map.of(), Map.of());
        Assertions.assertEquals(10.0, outputWithLinkedList.get("output").join().value());
    }

    @Test
    public void correctlyBehavesForIncorrectInputs() {
        Map<String, DataBox<?>> input0 = Map.of();
        var output0 = BasicNodes.LIST_LENGTH.compute(UUID.randomUUID(), input0, Map.of(), Map.of());
        Assertions.assertEquals(0.0, output0.get("output").join().value());
    }
}
