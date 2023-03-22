package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ObjectSetValueNodeTest {

    private static final class TestObjectSetValue {
        private final String firstKey = "Doctor";
        private final String secondKey = "Keppens";
    }

    private static final class TestObjectSetValueMember {
        private final String firstKey = "Prof";
    }

    private static final class TestObjectSetValueResult {
        private final String firstKey = "Prof";
        private final String secondKey = "Keppens";
    }

    private static final class TestObjectSetValueEmpty {
    }

    private static final Gson GSON = new Gson();

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.OBJECT_SET_VALUE);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("object"));
        Assertions.assertTrue(node.inputs().containsKey("element"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.OBJECT_SET_VALUE);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void canUpdateElement() {
        JsonObject object = GSON.fromJson(GSON.toJson(new TestObjectSetValue()), JsonObject.class);
        JsonElement element = GSON.fromJson(GSON.toJson(new TestObjectSetValueMember()), JsonObject.class).get("firstKey");
        String key = "firstKey";

        Map<String, DataBox<?>> settings = Map.of(
            "key", DataType.STRING.create(key)
        );

        Map<String, DataBox<?>> input = Map.of(
            "object", DataType.JSON_OBJECT.create(object),
            "element", DataType.JSON_ELEMENT.create(element)
        );

        // result:
        JsonObject result = GSON.fromJson(GSON.toJson(new TestObjectSetValueResult()), JsonObject.class);
        var output = BasicNodes.OBJECT_SET_VALUE.compute(UUID.randomUUID(), input, settings, Map.of());
        Assertions.assertEquals(result, output.get("output").join().value());
    }

    @Test
    public void canAddElement() {
        JsonObject object = GSON.fromJson(GSON.toJson(new TestObjectSetValueEmpty()), JsonObject.class);
        JsonElement element = GSON.fromJson(GSON.toJson(new TestObjectSetValueMember()), JsonObject.class).get("firstKey");
        String key = "firstKey";

        Map<String, DataBox<?>> settings = Map.of(
            "key", DataType.STRING.create(key)
        );

        Map<String, DataBox<?>> input = Map.of(
            "object", DataType.JSON_OBJECT.create(object),
            "element", DataType.JSON_ELEMENT.create(element)
        );

        // result:
        JsonObject result = GSON.fromJson(GSON.toJson(new TestObjectSetValueMember()), JsonObject.class);
        var output = BasicNodes.OBJECT_SET_VALUE.compute(UUID.randomUUID(), input, settings, Map.of());
        Assertions.assertEquals(result, output.get("output").join().value());
    }

    @Test
    public void wrongKeyHasNoEffect() {
        JsonObject object = GSON.fromJson(GSON.toJson(new TestObjectSetValueEmpty()), JsonObject.class);
        JsonElement element = GSON.fromJson(GSON.toJson(new TestObjectSetValueMember()), JsonObject.class).get("firstKey");
        String key = "";

        Map<String, DataBox<?>> settings = Map.of(
            "key", DataType.STRING.create(key)
        );

        Map<String, DataBox<?>> input = Map.of(
            "object", DataType.JSON_OBJECT.create(object),
            "element", DataType.JSON_ELEMENT.create(element)
        );

        // result:
        JsonObject result = GSON.fromJson(GSON.toJson(new TestObjectSetValueEmpty()), JsonObject.class);
        var output = BasicNodes.OBJECT_SET_VALUE.compute(UUID.randomUUID(), input, settings, Map.of());
        Assertions.assertEquals(result, output.get("output").join().value());
    }

}
