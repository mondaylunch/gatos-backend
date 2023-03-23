package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import club.mondaylunch.gatos.core.data.DataType;

import club.mondaylunch.gatos.core.data.OptionalDataType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.graph.Node;

public class EmptyOptionalNodeTest {
    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.EMPTY_OPTIONAL);
        Assertions.assertEquals(0, node.inputs().size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.EMPTY_OPTIONAL);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyGetsEmptyOptional() {
        var output = BasicNodes.EMPTY_OPTIONAL.compute(UUID.randomUUID(), Map.of(), BasicNodes.EMPTY_OPTIONAL.settings(), Map.of());
        Assertions.assertEquals(Optional.empty(), output.get("output").join().value());
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, output.get("output").join().type());
    }

    @Test
    public void correctlyGetsEmptyOptionalOfCorrectType() {
        var node = Node.create(BasicNodes.EMPTY_OPTIONAL).modifySetting("containing_type", DataType.DATA_TYPE.create(DataType.NUMBER));
        var output = BasicNodes.EMPTY_OPTIONAL.compute(UUID.randomUUID(), Map.of(), node.settings(), Map.of());
        Assertions.assertEquals(Optional.empty(), output.get("output").join().value());
        Assertions.assertEquals(DataType.NUMBER.optionalOf(), output.get("output").join().type());
    }
}
