package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class OptionalOrElseNodeTypeTest {
    @Test
    public void hasCorrectInputs() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.getInputWithName("optional").isPresent());
        Assertions.assertTrue(node.getInputWithName("fallback").isPresent());
    }

    @Test
    public void hasOutput() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyGetsTypeFromOptionalInput() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE)
            .updateInputTypes(Map.of("optional", DataType.STRING.optionalOf()));
        Assertions.assertEquals(DataType.STRING, node.getInputWithName("fallback").orElseThrow().type());
        Assertions.assertEquals(DataType.STRING, node.getOutputWithName("output").orElseThrow().type());
    }

    @Test
    public void correctlyGetsTypeFromFallbackInput() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE)
            .updateInputTypes(Map.of("fallback", DataType.STRING));
        Assertions.assertEquals(DataType.STRING.optionalOf(), node.getInputWithName("optional").orElseThrow().type());
        Assertions.assertEquals(DataType.STRING, node.getOutputWithName("output").orElseThrow().type());
    }

    @Test
    public void computesPresentOptionalCorrectly() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE)
            .updateInputTypes(Map.of("fallback", DataType.STRING));

        var output = BasicNodes.OPTIONAL_OR_ELSE.compute(
            UUID.randomUUID(), Map.of(
                "optional",
                DataType.STRING.optionalOf().create(Optional.of("Hello!")),
                "fallback",
                DataType.STRING.create("fallback")
            ),
            Map.of(),
            node.inputTypes()
        ).get("output").join().value();

        Assertions.assertEquals(
            "Hello!",
            output);
    }

    @Test
    public void computesAbsentOptionalCorrectly() {
        var node = Node.create(BasicNodes.OPTIONAL_OR_ELSE)
            .updateInputTypes(Map.of("fallback", DataType.STRING));

        var output = BasicNodes.OPTIONAL_OR_ELSE.compute(
            UUID.randomUUID(), Map.of(
                "optional",
                DataType.STRING.optionalOf().create(Optional.empty()),
                "fallback",
                DataType.STRING.create("fallback")
            ),
            Map.of(),
            node.inputTypes()
        ).get("output").join().value();

        Assertions.assertEquals(
            "fallback",
            output);
    }
}
