package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.graph.Node;

public class EmptyListNodeTest {
    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.EMPTY_LIST);
        Assertions.assertEquals(0, node.inputs().size());
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.EMPTY_LIST);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("output"));
    }

    @Test
    public void correctlyGetsEmptyList() {
        var output = BasicNodes.EMPTY_LIST.compute(UUID.randomUUID(), Map.of(), BasicNodes.EMPTY_LIST.settings(), Map.of());
        Assertions.assertEquals(List.of(), output.get("output").join().value());
        Assertions.assertEquals(ListDataType.GENERIC_LIST, output.get("output").join().type());
    }

    @Test
    public void correctlyGetsEmptyListOfCorrectType() {
        var node = Node.create(BasicNodes.EMPTY_LIST).modifySetting("containing_type", DataType.DATA_TYPE.create(DataType.NUMBER));
        var output = BasicNodes.EMPTY_LIST.compute(UUID.randomUUID(), Map.of(), node.settings(), Map.of());
        Assertions.assertEquals(List.of(), output.get("output").join().value());
        Assertions.assertEquals(DataType.NUMBER.listOf(), output.get("output").join().type());
    }
}
