package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class VariableRemappingNodeTest {
    @SuppressWarnings("unused")
    private static final class TestJSONRemappingClass {
        private final String firstKey = "Doctor";
        private final String secondKey = "Jeroen";
        private final String thirdKey = "Keppens";
    }

    private static final Gson GSON = new Gson();
    private static JsonObject TEST_JSON_OBJECT = GSON.fromJson(GSON.toJson(new TestJSONRemappingClass()), JsonObject.class);

    @AfterEach
    public void resetDefaultObject() {
        TEST_JSON_OBJECT = GSON.fromJson(GSON.toJson(new TestJSONRemappingClass()), JsonObject.class);
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.VARIABLE_REMAPPING);
        Assertions.assertEquals(3, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
        Assertions.assertTrue(node.inputs().containsKey("oldKey"));
        Assertions.assertTrue(node.inputs().containsKey("newKey"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.VARIABLE_REMAPPING);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyRemapsKeys() {
        Map<String, DataBox<?>> input0 = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "oldKey", DataType.STRING.create("firstKey"),
            "newKey", DataType.STRING.create("title")
        );
        var result0 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input0, Map.of(), Map.of());

        Map<String, DataBox<?>> input1 = Map.of(
            "input", DataType.JSON_OBJECT.create((JsonObject) result0.get("output").join().value()),
            "oldKey", DataType.STRING.create("secondKey"),
            "newKey", DataType.STRING.create("firstName")
        );
        var result1 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input1, Map.of(), Map.of());

        Map<String, DataBox<?>> input2 = Map.of(
            "input", DataType.JSON_OBJECT.create((JsonObject) result1.get("output").join().value()),
            "oldKey", DataType.STRING.create("thirdKey"),
            "newKey", DataType.STRING.create("lastName")
        );
        var result2 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input2, Map.of(), Map.of());

        Assertions.assertTrue(result2.get("output").join().value() instanceof JsonObject jsonObject
            && jsonObject.get("title").getAsString().equals("Doctor")
            && jsonObject.get("firstName").getAsString().equals("Jeroen")
            && jsonObject.get("lastName").getAsString().equals("Keppens"));
    }

    @Test
    public void doesNotRemapInvalidKeys() {
        Map<String, DataBox<?>> input0 = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "newKey", DataType.STRING.create("title")
        );
        var result0 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input0, Map.of(), Map.of());
        Assertions.assertEquals(result0.get("output").join().value(), TEST_JSON_OBJECT);

        Map<String, DataBox<?>> input1 = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "oldKey", DataType.STRING.create("firstKey")
        );
        var result1 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input1, Map.of(), Map.of());
        Assertions.assertEquals(result1.get("output").join().value(), TEST_JSON_OBJECT);

        Map<String, DataBox<?>> input2 = Map.of(
            "oldKey", DataType.STRING.create("firstKey"),
            "newKey", DataType.STRING.create("title")
        );
        var result2 = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input2, Map.of(), Map.of());
        Assertions.assertEquals(result2.get("output").join().value(), new JsonObject());
    }

    @Test
    public void doesNotRemapSameKeys() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "oldKey", DataType.STRING.create("firstKey"),
            "newKey", DataType.STRING.create("firstKey")
        );
        var result = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(result.get("output").join().value(), TEST_JSON_OBJECT);
    }

    @Test
    public void doesNotRemapIncorrectKeys() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "oldKey", DataType.STRING.create("thisDoesNotExist"),
            "newKey", DataType.STRING.create("title")
        );
        var result = BasicNodes.VARIABLE_REMAPPING.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(result.get("output").join().value(), TEST_JSON_OBJECT);
    }
}
