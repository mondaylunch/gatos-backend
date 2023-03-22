package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class EqualsNodeTest {

    private static final class TestEqualsNodeFirstJSON {
        private final String firstKey = "Doctor";
        private final String secondKey = "Jeroen";
        private final String thirdKey = "Keppens";
    }

    private static final class TestEqualsNodeSecondJSON {
        private final String firstKey = "Doctor";
        private final String secondKey = "Keppens";
    }

    private static final Gson GSON = new Gson();

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.EQUALS);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("inputA"));
        Assertions.assertTrue(node.inputs().containsKey("inputB"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.EQUALS);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void identifiesEqualStrings() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create("test_string"),
            "inputB", DataType.ANY.create("test_string")
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualStrings() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create("test_string"),
            "inputB", DataType.ANY.create("this is not equal to the first string")
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualNumbers() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(0.0),
            "inputB", DataType.ANY.create(0.0)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualNumbers() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(1.0),
            "inputB", DataType.ANY.create(0.0)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualBooleans() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(true),
            "inputB", DataType.ANY.create(true)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualBooleans() {
        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(true),
            "inputB", DataType.ANY.create(false)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualLists() {
        List<Integer> firstObject = new ArrayList<>();
        List<Integer> secondObject = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            firstObject.add(i);
            secondObject.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(firstObject),
            "inputB", DataType.ANY.create(secondObject)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualLists() {
        List<Integer> firstObject = new ArrayList<>();
        List<Integer> secondObject = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            firstObject.add(i);
            secondObject.add(i + 23);
        }

        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(firstObject),
            "inputB", DataType.ANY.create(secondObject)
        );
        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualObjects() {
        List<Integer> firstObject = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            firstObject.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(firstObject),
            "inputB", DataType.ANY.create(true)
        );
        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualJSON() {
        JsonObject TEST_JSON_OBJECT_1 = GSON.fromJson(GSON.toJson(new TestEqualsNodeFirstJSON()), JsonObject.class);
        JsonObject TEST_JSON_OBJECT_2 = GSON.fromJson(GSON.toJson(new TestEqualsNodeSecondJSON()), JsonObject.class);

        Map<String, DataBox<?>> input_1 = Map.of(
            "inputA", DataType.ANY.create(TEST_JSON_OBJECT_1),
            "inputB", DataType.ANY.create(TEST_JSON_OBJECT_1)
        );

        Map<String, DataBox<?>> input_2 = Map.of(
            "inputA", DataType.ANY.create(TEST_JSON_OBJECT_2),
            "inputB", DataType.ANY.create(TEST_JSON_OBJECT_2)
        );

        var output_1 = BasicNodes.EQUALS.compute(UUID.randomUUID(), input_1, Map.of(), Map.of());
        Assertions.assertEquals(true, output_1.get("output").join().value());

        var output_2 = BasicNodes.EQUALS.compute(UUID.randomUUID(), input_2, Map.of(), Map.of());
        Assertions.assertEquals(true, output_2.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualJSON() {
        JsonObject TEST_JSON_OBJECT_1 = GSON.fromJson(GSON.toJson(new TestEqualsNodeFirstJSON()), JsonObject.class);
        JsonObject TEST_JSON_OBJECT_2 = GSON.fromJson(GSON.toJson(new TestEqualsNodeSecondJSON()), JsonObject.class);

        Map<String, DataBox<?>> input = Map.of(
            "inputA", DataType.ANY.create(TEST_JSON_OBJECT_1),
            "inputB", DataType.ANY.create(TEST_JSON_OBJECT_2)
        );

        var output = BasicNodes.EQUALS.compute(UUID.randomUUID(), input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

}
