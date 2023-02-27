package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class EqualsNodeTest {
    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.EQUALS);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("first_object"));
        Assertions.assertTrue(node.inputs().containsKey("second_object"));
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
            "first_object", DataType.ANY.create("test_string"),
            "second_object", DataType.ANY.create("test_string")
        );

        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualStrings() {
        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create("test_string"),
            "second_object", DataType.ANY.create("this is not equal to the first string")
        );

        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualNumbers() {
        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create(0.0),
            "second_object", DataType.ANY.create(0.0)
        );

        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualNumbers() {
        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create(1.0),
            "second_object", DataType.ANY.create(0.0)
        );
        
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesEqualBooleans() {
        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create(true),
            "second_object", DataType.ANY.create(true)
        );
        
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(true, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualBooleans() {
        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create(true),
            "second_object", DataType.ANY.create(false)
        );
        
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
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
            "first_object", DataType.ANY.create(firstObject),
            "second_object", DataType.ANY.create(secondObject)
        );
        
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
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
            "first_object", DataType.ANY.create(firstObject),
            "second_object", DataType.ANY.create(secondObject)
        );
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }

    @Test
    public void identifiesNotEqualObjects() {
        List<Integer> firstObject = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            firstObject.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "first_object", DataType.ANY.create(firstObject),
            "second_object", DataType.ANY.create(true)
        );
        var output = BasicNodes.EQUALS.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(false, output.get("output").join().value());
    }
}
