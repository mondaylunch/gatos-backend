package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class GetAtIndexNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
        Assertions.assertTrue(node.inputs().containsKey("index"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesEmptyLists() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(0.0)
        );
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(Optional.empty(), output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesIndexOutOfRange() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(6.0)
        );
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(Optional.empty(), output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLists() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataBox<?>> input2 = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(1.0)
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(Optional.of(0), output.get("output").join().value());

        var output2 = BasicNodes.GET_AT_INDEX.compute(input2, Map.of(), Map.of());
        Assertions.assertEquals(Optional.of(1), output2.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesReturnType() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.NUMBER
        );

        var node = Node.create(BasicNodes.GET_AT_INDEX).updateInputTypes(inputTypes);
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(Optional.of(0), output.get("output").join().value());
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, node.getOutputWithName("output").orElseThrow().type());
    }

    @Test
    public void correctlyEvaluatesReturnTypeWithWrongInput() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.NUMBER
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(Optional.empty(), output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLinkedLists() {
        List<String> testLinkedList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            testLinkedList.add("i" + i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testLinkedList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.STRING
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(Optional.of("i0"), output.get("output").join().value());
    }

}
