package club.mondaylunch.gatos.basicnodes.test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyGetsEmptyList() {
        var output = BasicNodes.EMPTY_LIST.compute(UUID.randomUUID(), Map.of(), Map.of(), Map.of());
        Assertions.assertEquals(List.of(), output.get("output").join().value());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cannotModifyEmptyList() {
        var output = BasicNodes.EMPTY_LIST.compute(UUID.randomUUID(), Map.of(), Map.of(), Map.of());
        var createdList = (List<String>) output.get("output").join().value();
        Assertions.assertThrows(Exception.class, () -> {
            createdList.add("turi ip ip ip");
            createdList.add("ip ip ip ip tsha ik");
            createdList.add("ip tsha ip ik");
        });
    }
}
