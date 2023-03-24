package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;

public class ParseStringToObjectNodeTest {
    @SuppressWarnings("unused")
    private static final class TestJSONClass {
        private final int salt = 112150914;
        private final int spacing = 128;
        private final int separation = 64;
        private final String type = "random_spread";
    }

    private static final String GOOD_GEESON = new Gson().toJson(new TestJSONClass());
    private static final String BAD_GEESON = "{{thisIsNotValid}";

    @Test
    public void canAddNodeToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.PARSE_STRING_TO_OBJECT);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING_TO_OBJECT);
        Assertions.assertEquals(node.inputs().size(), 1);
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.PARSE_STRING_TO_OBJECT);
        Assertions.assertEquals(node.outputs().size(), 1);
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctParsesValidString() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(GOOD_GEESON)
        );
        var output = (JsonObject) BasicNodes.PARSE_STRING_TO_OBJECT.compute(input).get("output").join().value();
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(Set.of("salt", "spacing", "separation", "type"), output.keySet());
    }

    @Test
    public void correctParsesInvalidString() {
        Map<String, DataBox<?>> input = Map.of(
            "input", DataType.STRING.create(BAD_GEESON)
        );
        var output = (JsonObject) BasicNodes.PARSE_STRING_TO_OBJECT.compute(input).get("output").join().value();
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(Set.of("value"), output.keySet());
    }
}
