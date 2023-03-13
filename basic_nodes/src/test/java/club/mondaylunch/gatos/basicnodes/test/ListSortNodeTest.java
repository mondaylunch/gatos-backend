package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListSortNodeTest {
    private static final List<Double> TEST_NUM_LIST = Arrays.asList(3., 2., 4., 5., 1.);
    private static final List<String> TEST_STR_LIST = Arrays.asList("e", "b", "a", "d", "c");
    private static final List<Boolean> TEST_BOO_LIST = Arrays.asList(false, true, false, true, false);

    private static final class TestJSONObjectClass {
        private final double sampleInt = 23;
        private final boolean sampleBoolean = true;
    }

    private static final Gson GSON = new Gson();
    private static final List<JsonObject> TEST_JSN_LIST = Arrays.asList(new JsonObject(), GSON.fromJson(GSON.toJson(new TestJSONObjectClass()), JsonObject.class));

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlySortsNumberList() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var output = BasicNodes.LIST_SORT.compute(input, node.settings(),
            Map.of("input", DataType.NUMBER.listOf()));
        Assertions.assertEquals(List.of(1., 2., 3., 4., 5.), output.get("output").join().value());
    }

    @Test
    public void correctlySortsStringList() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_STR_LIST)
        );
        var output = BasicNodes.LIST_SORT.compute(input, node.settings(),
            Map.of("input", DataType.STRING.listOf()));
        Assertions.assertEquals(List.of("a", "b", "c", "d", "e"), output.get("output").join().value());
    }

    @Test
    public void correctlySortsBooleanList() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_BOO_LIST)
        );
        var output = BasicNodes.LIST_SORT.compute(input, node.settings(),
            Map.of("input", DataType.BOOLEAN.listOf()));
        Assertions.assertEquals(List.of(false, false, false, true, true), output.get("output").join().value());
    }

    @Test
    public void doesntSortUncomparableTypes() {
        var node = Node.create(BasicNodes.LIST_SORT);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_JSN_LIST)
        );
        var output = BasicNodes.LIST_SORT.compute(input, node.settings(),
            Map.of("input", DataType.JSON_OBJECT.listOf()));
        Assertions.assertEquals(TEST_JSN_LIST, output.get("output").join().value());
    }
}
