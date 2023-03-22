package club.mondaylunch.gatos.basicnodes.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.process.VariableExtractionNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class VariableExtractionNodeTest {
    private static final class TestJSONExtractionClass {
        private final double testNumber = 420;
        private final boolean testBoolean = true;
        private final String testString = "tickles";
        private final Collection<Double> testNumberCollection = List.of(0., 1., 2., 3.);
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
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyExtractsNumber() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.NUMBER));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testNumber")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(Optional.of(new TestJSONExtractionClass().testNumber), result.get("output").join().value());
    }

    @Test
    public void correctlyExtractsBoolean() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.BOOLEAN));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testBoolean")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(Optional.of(new TestJSONExtractionClass().testBoolean), result.get("output").join().value());
    }

    @Test
    public void correctlyExtractsString() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.STRING));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testString")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(Optional.of(new TestJSONExtractionClass().testString), result.get("output").join().value());
    }

    @Test
    public void correctlyExtractsCollection() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.NUMBER.listOf()));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testNumberCollection")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(new TestJSONExtractionClass().testNumberCollection, result.get("output").join().value());

        node.modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.STRING.listOf()));
        Map<String, DataBox<?>> inputStr = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testStrCollection")
        );
        var resultStr = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), inputStr, node.settings(), Map.of());
        Assertions.assertEquals(new TestJSONExtractionClass().testStrCollection, resultStr.get("output").join().value());

        node.modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.JSON_OBJECT.listOf()));
        Map<String, DataBox<?>> inputJson = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testJsonObjCollection")
        );
        var resultJson = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), inputJson, node.settings(), Map.of());
        Assertions.assertEquals(new TestJSONExtractionClass().testJsonObjCollection, resultJson.get("output").join().value());
    }

    @Test
    public void correctlyExtractsIndividualAsCollection() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION)
            .modifySetting("output_type", VariableExtractionNodeType.getReturnBoxFromType(DataType.NUMBER.listOf()));
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("testNumber")
        );
        var result = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(List.of(new TestJSONExtractionClass().testNumber), result.get("output").join().value());
    }

    @Test
    public void extractMissingValuesAsEmpty() {
        var node = Node.create(BasicNodes.VARIABLE_EXTRACTION);
        Map<String, DataBox<?>> input0 = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT),
            "key", DataType.STRING.create("notAValidKey")
        );
        var result0 = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input0, node.settings(), Map.of());
        Assertions.assertEquals(Optional.empty(), result0.get("output").join().value());

        Map<String, DataBox<?>> input1 = Map.of(
            "key", DataType.STRING.create("testNumber")
        );
        var result1 = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input1, node.settings(), Map.of());
        Assertions.assertEquals(Optional.empty(), result1.get("output").join().value());

        Map<String, DataBox<?>> input2 = Map.of(
            "input", DataType.JSON_OBJECT.create(TEST_JSON_OBJECT)
        );
        var result2 = BasicNodes.VARIABLE_EXTRACTION.compute(UUID.randomUUID(), input2, node.settings(), Map.of());
        Assertions.assertEquals(Optional.empty(), result2.get("output").join().value());
    }
}
