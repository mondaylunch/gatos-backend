package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class OptionalFilterNodeTest {
    private static final Map<Object, DataBox<?>> sampleValues = Map.of(
        "test", DataType.STRING.create("test"),
        1.0, DataType.NUMBER.create(1.0),
        true, DataType.BOOLEAN.create(true),
        new JsonObject(), DataType.JSON_OBJECT.create(new JsonObject())
    );

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.OPTIONAL_FILTER);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("data"));
        Assertions.assertTrue(node.inputs().containsKey("conditional"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.OPTIONAL_FILTER);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void filtersTrueOptionals() {
        for (var entry : sampleValues.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue().type();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of(
                "data", dataType.create(data),
                "conditional", DataType.BOOLEAN.create(true)
            );
            var output = BasicNodes.OPTIONAL_FILTER.compute(
                UUID.randomUUID(), input, Map.of(), Map.of("data", dataType)
            );
            var out = output.get("output").join();
            Assertions.assertEquals(Optional.of(entry.getKey()), out.value());
            Assertions.assertEquals(entry.getValue().type().optionalOf(), out.type());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void filtersFalseOptionals() {
        for (var entry : sampleValues.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue().type();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of(
                "data", dataType.create(data),
                "conditional", DataType.BOOLEAN.create(false)
            );
            var output = BasicNodes.OPTIONAL_FILTER.compute(
                UUID.randomUUID(), input, Map.of(), Map.of("data", dataType)
            );
            var out = output.get("output").join();
            Assertions.assertEquals(Optional.empty(), out.value());
            Assertions.assertEquals(entry.getValue().type().optionalOf(), out.type());
        }
    }

    @Test
    public void correctlySpecialisesTypes() {
        for (var entry : sampleValues.entrySet()) {
            var node = Node.create(BasicNodes.OPTIONAL_FILTER)
                .updateInputTypes(Map.of("data", entry.getValue().type()));
            Assertions.assertEquals(entry.getValue().type().optionalOf(), node.getOutputWithName("output").orElseThrow().type());
        }
    }
}
