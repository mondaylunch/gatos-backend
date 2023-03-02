package club.mondaylunch.gatos.basicnodes.test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class TruthinessNodeTest {
    private static final Map<Object, DataType<?>> TRUTHY_VALUES = Map.of(
        1.0, DataType.NUMBER,
        "1", DataType.STRING,
        true, DataType.BOOLEAN,
        new JsonObject(), DataType.JSON_OBJECT,
        List.of(), DataType.NUMBER.listOf(),
        Optional.of("leonardo"), DataType.STRING.optionalOf()
    );
    private static final Map<Object, DataType<?>> FALSY_VALUES = Map.of(
        0.0, DataType.NUMBER,
        "", DataType.STRING,
        false, DataType.BOOLEAN,
        Optional.of(0.0), DataType.NUMBER.optionalOf(),
        Optional.of(""), DataType.STRING.optionalOf()
    );

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.TRUTHINESS);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input_data"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.TRUTHINESS);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyEvaluatesTruthyValues() {
        for (var entry : TRUTHY_VALUES.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of("input_data", dataType.create(data));
            var result =
                BasicNodes.TRUTHINESS.compute(input, Map.of(), Map.of("input", dataType));
            Assertions.assertTrue((boolean) result.get("output").join().value());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyEvaluatesFalsyValues() {
        for (var entry : FALSY_VALUES.entrySet()) {
            var dataType = (DataType<Object>) entry.getValue();
            var data = entry.getKey();
            Map<String, DataBox<?>> input = Map.of("input_data", dataType.create(data));
            var result =
                BasicNodes.TRUTHINESS.compute(input, Map.of(), Map.of("input", dataType));
            Assertions.assertFalse((boolean) result.get("output").join().value());
        }
    }
}
