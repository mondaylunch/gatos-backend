package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class GetAtIndexNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
        Assertions.assertTrue(node.inputs().containsKey("index"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesNullLists() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(0.0)
        );
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLists() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataBox<?>> input2 = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(1.0)
        );
        
        Map<String, DataType<?>> inputTypes = Map.of(
            "input_type", DataType.NUMBER
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(0, output.get("output").join().value());
        Assertions.assertEquals(output.get("output").join().type(), DataType.NUMBER);

        var output2 = BasicNodes.GET_AT_INDEX.compute(input2, Map.of(), inputTypes);
        Assertions.assertEquals(1, output2.get("output").join().value());
    }
}
