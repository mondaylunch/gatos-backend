package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListTailSeparationNodeTest {
    private static final List<Number> TEST_NUM_LIST = List.of(0, 0, 1.765, 6.969, 4.20F);
    private static final List<String> TEST_STR_LIST = List.of("CHEESE");

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_TAIL_SEPARATION);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_TAIL_SEPARATION);
        Assertions.assertEquals(2, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("first"));
        Assertions.assertTrue(node.outputs().containsKey("rest"));
    }

    @Test
    public void correctlySeparatesInTailMode() {
        var node = Node.create(BasicNodes.LIST_TAIL_SEPARATION);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var output = BasicNodes.LIST_TAIL_SEPARATION.compute(UUID.randomUUID(), input, node.settings(),
            Map.of("input", DataType.NUMBER));
        Assertions.assertEquals(TEST_NUM_LIST.get(TEST_NUM_LIST.size() - 1), output.get("first").join().value());
        Assertions.assertEquals(TEST_NUM_LIST.subList(0, TEST_NUM_LIST.size() - 1), output.get("rest").join().value());
    }

    @Test
    public void correctlyExtractsSizeOneLists() {
        var node = Node.create(BasicNodes.LIST_TAIL_SEPARATION);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_STR_LIST)
        );
        var output = BasicNodes.LIST_TAIL_SEPARATION.compute(UUID.randomUUID(), input, node.settings(),
            Map.of("input", DataType.STRING));
        Assertions.assertEquals(TEST_STR_LIST.get(0), output.get("first").join().value());
        Assertions.assertEquals(List.of(), output.get("rest").join().value());
    }
}
