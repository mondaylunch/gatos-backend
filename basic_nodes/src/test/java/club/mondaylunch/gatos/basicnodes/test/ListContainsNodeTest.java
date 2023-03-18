package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListContainsNodeTest {
    private static final List<Double> TEST_NUM_LIST = Arrays.asList(1., 3., 7., 21.);
    private static final List<String> TEST_STR_LIST = Arrays.asList("z", "y", "x", "w", "v");

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_CONTAINS);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("list"));
        Assertions.assertTrue(node.inputs().containsKey("element"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_CONTAINS);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyFindsIfElementPresent() {
        var node = Node.create(BasicNodes.LIST_CONTAINS);
        Map<String, DataBox<?>> dataInputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "element", DataType.ANY.create(21.)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            "list", DataType.NUMBER.listOf(),
            "element", DataType.NUMBER
        );
        var output = BasicNodes.LIST_CONTAINS.compute(dataInputs, node.settings(), typeInputs);
        Assertions.assertTrue((boolean) output.get("output").join().value());
    }

    @Test
    public void correctlyFindsIfElementAbsent() {
        var node = Node.create(BasicNodes.LIST_CONTAINS);
        Map<String, DataBox<?>> dataInputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST),
            "element", DataType.ANY.create(69.)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            "list", DataType.NUMBER.listOf(),
            "element", DataType.NUMBER
        );
        var output = BasicNodes.LIST_CONTAINS.compute(dataInputs, node.settings(), typeInputs);
        Assertions.assertFalse((boolean) output.get("output").join().value());
    }

    @Test
    public void throwsExceptionIfTypesMismatch() {
        var node = Node.create(BasicNodes.LIST_CONTAINS);
        Map<String, DataBox<?>> dataInputs = Map.of(
            "list", ListDataType.GENERIC_LIST.create(TEST_STR_LIST),
            "element", DataType.ANY.create(420.)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            "list", DataType.STRING.listOf(),
            "element", DataType.NUMBER
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            BasicNodes.LIST_CONTAINS.compute(dataInputs, node.settings(), typeInputs)
        );
    }
}
