package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListDistinctNodeTest {
    private static final List<Double> TEST_NUM_LIST = Arrays.asList(4., 4., 1., 2., 3., 3., 3.);
    private static final List<Boolean> TEST_BOO_LIST = Arrays.asList(false, false, false, true, true);
    private static final List<JsonObject> TEST_JSN_LIST = Arrays.asList(new JsonObject(), new JsonObject());

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_DISTINCT);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_DISTINCT);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyMakesDistinctListAndPreservesOrder() {
        var node = Node.create(BasicNodes.LIST_DISTINCT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var output = BasicNodes.LIST_DISTINCT.compute(input, node.settings(),
            Map.of("input", DataType.NUMBER.listOf()));
        Assertions.assertEquals(List.of(4., 1., 2., 3.), output.get("output").join().value());
        Assertions.assertNotEquals(List.of(1., 2., 3., 4.), output.get("output").join().value());
    }

    @Test
    public void correctlySpecialisesList() {
        var node = Node.create(BasicNodes.LIST_DISTINCT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var inputType = DataType.NUMBER.listOf();
        var output = BasicNodes.LIST_DISTINCT.compute(input, node.settings(),
            Map.of("input", inputType));
        var outputType = output.get("output").join().type();
        Assertions.assertEquals(inputType, outputType);
    }

    @Test
    public void outputListIsDifferentFromOriginal() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_BOO_LIST)
        );
        var output = BasicNodes.LIST_DISTINCT.compute(input, node.settings(),
            Map.of("input", DataType.BOOLEAN.listOf()));
        var outputList = output.get("output").join().value();
        Assertions.assertEquals(List.of(false, true), outputList);
        Assertions.assertNotEquals(TEST_BOO_LIST, outputList);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void outputListIsImmutable() {
        var node = Node.create(BasicNodes.LIST_REVERSE);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_JSN_LIST)
        );
        var output = BasicNodes.LIST_DISTINCT.compute(input, node.settings(),
            Map.of("input", DataType.JSON_OBJECT.listOf()));
        var outputList = (List<JsonObject>) output.get("output").join().value();
        Assertions.assertEquals(List.of(new JsonObject()), outputList);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> outputList.add(new JsonObject()));
    }
}
