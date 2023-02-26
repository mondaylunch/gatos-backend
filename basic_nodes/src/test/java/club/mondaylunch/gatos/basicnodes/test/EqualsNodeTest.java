package club.mondaylunch.gatos.basicnodes.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
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

}
