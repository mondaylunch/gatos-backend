package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListReverseNodeTest {
    private static final List<Double> TEST_NUM_LIST = Arrays.asList(1., 2., 3., 26., 4., 5.);
    private static final List<String> TEST_STR_LIST = Arrays.asList("a", "b", "c", "z", "d", "e");

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyReversesAndSpecialisesLists() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var inputType = DataType.NUMBER.listOf();
        var output = BasicNodes.LIST_REVERSE.compute(input, node.settings(),
            Map.of("input", inputType));
        var outputType = output.get("output").join().type();
        Assertions.assertEquals(List.of(5., 4., 26., 3., 2., 1.), output.get("output").join().value());
        Assertions.assertEquals(inputType, outputType);
    }

    @Test
    public void outputListIsDifferentFromOriginal() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_STR_LIST)
        );
        var output = BasicNodes.LIST_REVERSE.compute(input, node.settings(),
            Map.of("input", DataType.STRING.listOf()));
        var outputList = output.get("output").join().value();
        Assertions.assertEquals(List.of("e", "d", "z", "c", "b", "a"), outputList);
        Assertions.assertNotEquals(TEST_STR_LIST, outputList);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void outputListIsImmutable() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_STR_LIST)
        );
        var output = BasicNodes.LIST_REVERSE.compute(input, node.settings(),
            Map.of("input", DataType.STRING.listOf()));
        var outputList = (List<String>) output.get("output").join().value();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> outputList.add("f"));
    }
}
