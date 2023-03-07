package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ToStringNodeTest {
    
    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.TO_STRING);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("data"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.TO_STRING);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void convertStringCorrectly() {
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create("test_string")
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("test_string", output.get("output").join().value());
    }

    @Test
    public void convertNumbersCorrectly() {
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(123.2)
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("123.2", output.get("output").join().value());
    }

    @Test
    public void convertBooleansCorrectly() {
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(true)
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("true", output.get("output").join().value());
    }

    @Test
    public void convertOptionalsCorrectly() {
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(DataType.ANY.create(Optional.of("d")))
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("d", output.get("output").join().value());
    }

    @Test
    public void convertEmptyOptionalsCorrectly() {
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(DataType.ANY.create(Optional.empty()))
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("", output.get("output").join().value());
    }

    @Test
    public void convertListsCorrectly() {
        List<Integer> testList = new ArrayList<>(List.of(1, 2));
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(ListDataType.GENERIC_LIST.create(testList))
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("[1, 2]", output.get("output").join().value());
    }

    @Test
    public void convertEmptyListsCorrectly() {
        List<Integer> testList = new ArrayList<>();
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(ListDataType.GENERIC_LIST.create(testList))
        );

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("[]", output.get("output").join().value());
    }

    @Test
    public void convertMapsCorrectly() {
        Map<Integer, String> data = Map.of(123, "hello");
        Map<String, DataBox<?>> input = Map.of(
            "data", DataType.ANY.create(data)
        );

        var emptyOutput = BasicNodes.TO_STRING.compute(Map.of(), Map.of(), Map.of());
        Assertions.assertEquals("", emptyOutput.get("output").join().value());

        var output = BasicNodes.TO_STRING.compute(input, Map.of(), Map.of());
        Assertions.assertEquals("{123=hello}", output.get("output").join().value());
    }
}