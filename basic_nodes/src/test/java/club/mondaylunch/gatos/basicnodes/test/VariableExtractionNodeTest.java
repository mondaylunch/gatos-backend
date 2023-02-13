package club.mondaylunch.gatos.basicnodes.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class VariableExtractionNodeTest {
    private static final class TestJSONExtractionClass {
        private final int testInt = 420;
        private final boolean testBoolean = true;
        private final String testString = "tickles";
        private final Collection<Integer> testCollection = List.of(0, 1, 2, 3);
    }

    private static final Gson GSON = new Gson();
    private static final JsonObject TEST_JSON_OBJECT = GSON.fromJson(GSON.toJson(new TestJSONExtractionClass()),
            JsonObject.class);

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
        Assertions.assertTrue(node.inputs().containsKey("key"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyExtractsInt() {
        Map<String, DataBox<?>> input = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
                "key", DataType.STRING.create("testInt"));
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, Map.of());
        Assertions.assertTrue(result.get("output").join().value() instanceof JsonPrimitive json
                && json.isNumber() && json.getAsInt() == 420);
    }

    @Test
    public void correctlyExtractsBoolean() {
        Map<String, DataBox<?>> input = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
                "key", DataType.STRING.create("testBoolean"));
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, Map.of());
        Assertions.assertTrue(result.get("output").join().value() instanceof JsonPrimitive json
                && json.isBoolean() && json.getAsBoolean());
    }

    @Test
    public void correctlyExtractsString() {
        Map<String, DataBox<?>> input = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
                "key", DataType.STRING.create("testString"));
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, Map.of());
        Assertions.assertTrue(result.get("output").join().value() instanceof JsonPrimitive json
                && json.isString() && json.getAsString().equals("tickles"));
    }

    @Test
    public void correctlyExtractsCollection() {
        Map<String, DataBox<?>> input = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
                "key", DataType.STRING.create("testCollection"));
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, Map.of());
        var expectedResult = new JsonArray();
        for (int i = 0; i < 4; ++i) {
            expectedResult.add(i);
        }
        Assertions.assertEquals(result.get("output").join().value(), expectedResult);
    }

    @Test
    public void extractMissingValuesAsNull() {
        Map<String, DataBox<?>> input0 = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
                "key", DataType.STRING.create("notAValidKey"));
        var result0 = BasicNodes.VARIABLE_EXTRACTION.compute(input0, Map.of());
        Assertions.assertEquals(result0.get("output").join().value(), JsonNull.INSTANCE);

        Map<String, DataBox<?>> input1 = Map.of(
                "key", DataType.STRING.create("testInt"));
        var result1 = BasicNodes.VARIABLE_EXTRACTION.compute(input1, Map.of());
        Assertions.assertEquals(result1.get("output").join().value(), JsonNull.INSTANCE);

        Map<String, DataBox<?>> input2 = Map.of(
                "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT));
        var result2 = BasicNodes.VARIABLE_EXTRACTION.compute(input2, Map.of());
        Assertions.assertEquals(result2.get("output").join().value(), JsonNull.INSTANCE);
    }
}
