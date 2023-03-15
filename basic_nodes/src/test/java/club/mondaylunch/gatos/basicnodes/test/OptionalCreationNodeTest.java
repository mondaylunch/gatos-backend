package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class OptionalCreationNodeTest {
    private static final Map<Object, DataBox<?>> sampleValues = Map.of(
        "test", DataType.STRING.create("test"),
        1.0, DataType.NUMBER.create(1.0),
        true, DataType.BOOLEAN.create(true),
        new JsonObject(), DataType.JSON_OBJECT.create(new JsonObject())
    );

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.OPTIONAL_CREATION);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.OPTIONAL_CREATION);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyGetsOptionalValues() {
        for (var entry : sampleValues.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue().type();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of("input", dataType.create(data));
            var output = BasicNodes.OPTIONAL_CREATION.compute(
                input, Map.of(), Map.of("input", dataType)
            );
            Assertions.assertEquals(Optional.of(entry.getKey()), output.get("output").join().value());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void outputsWithCorrectTypes() {
        for (var entry : sampleValues.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue().type();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of("input", dataType.create(data));
            var output = BasicNodes.OPTIONAL_CREATION.compute(
                input, Map.of(), Map.of("input", dataType)
            );
            Assertions.assertEquals(entry.getValue().type().optionalOf(), output.get("output").join().type());
        }
    }
}
