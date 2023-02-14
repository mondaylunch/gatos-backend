package gay.oss.gatos.basicnodes.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.basicnodes.BasicNodes;
import gay.oss.gatos.basicnodes.VariableExtractionNodeType;
import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.graph.Node;

public class VariableExtractionNodeTest {
    private static final class TestJSONExtractionClass {
        private final int testInt = 420;
        private final boolean testBoolean = true;
        private final String testString = "tickles";
        private final Collection<Integer> testIntCollection = List.of(0, 1, 2, 3);
        private final Collection<String> testStrCollection = List.of("b", "r", "u", "h");
        private final Collection<JsonObject> testJsonObjCollection = List.of(new JsonObject(), new JsonObject());
    }

    private static final Gson GSON = new Gson();
    private static final JsonObject TEST_JSON_OBJECT = GSON.fromJson(GSON.toJson(new TestJSONExtractionClass()), JsonObject.class);

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
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.INTEGER));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testInt")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, node.settings());
        Assertions.assertEquals(result.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testInt));
    }

    @Test
    public void correctlyExtractsBoolean() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.BOOLEAN));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testBoolean")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, node.settings());
        Assertions.assertEquals(result.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testBoolean));
    }

    @Test
    public void correctlyExtractsString() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.STRING));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testString")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, node.settings());
        Assertions.assertEquals(result.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testString));
    }

    @Test
    public void correctlyExtractsCollection() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.INTEGER.listOf()));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testIntCollection")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, node.settings());
        Assertions.assertEquals(result.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testIntCollection));

        node.modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.STRING.listOf()));
        Map<String, DataBox<?>> inputStr = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testStrCollection")
        );
        var resultStr = BasicNodes.VARIABLE_EXTRACTION.compute(inputStr, node.settings());
        Assertions.assertEquals(resultStr.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testStrCollection));

        node.modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.JSONOBJECT.listOf()));
        Map<String, DataBox<?>> inputJson = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testJsonObjCollection")
        );
        var resultJson = BasicNodes.VARIABLE_EXTRACTION.compute(inputJson, node.settings());
        Assertions.assertEquals(resultJson.get("output").join().value(), Optional.of(new TestJSONExtractionClass().testJsonObjCollection));
    }

    @Test
    public void correctlyExtractsIndividualAsCollection() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.INTEGER.listOf()));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testInt")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(input, node.settings());
        Assertions.assertEquals(result.get("output").join().value(), Optional.of(List.of(new TestJSONExtractionClass().testInt)));
    }

    @Test
    public void extractMissingValuesAsEmpty() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION);
        Map<String, DataBox<?>> input0 = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("notAValidKey")
        );
        var result0 = BasicNodes.VARIABLE_EXTRACTION.compute(input0, node.settings());
        Assertions.assertEquals(result0.get("output").join().value(), Optional.empty());

        Map<String, DataBox<?>> input1 = Map.of(
            "key", DataType.STRING.create("testInt")
        );
        var result1 = BasicNodes.VARIABLE_EXTRACTION.compute(input1, node.settings());
        Assertions.assertEquals(result1.get("output").join().value(), Optional.empty());

        Map<String, DataBox<?>> input2 = Map.of(
            "input", DataType.JSONOBJECT.create(TEST_JSON_OBJECT)
        );
        var result2 = BasicNodes.VARIABLE_EXTRACTION.compute(input2, node.settings());
        Assertions.assertEquals(result2.get("output").join().value(), Optional.empty());
    }
}