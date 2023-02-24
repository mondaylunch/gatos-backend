package club.mondaylunch.gatos.basicnodes.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import club.mondaylunch.gatos.basicnodes.ListHeadTailNodeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ListHeadTailNodeTest {
    private static final List<Number> TEST_NUM_LIST = List.of(0, 0, 1.765, 6.969, 4.20F);
    private static final List<String> TEST_STR_LIST = List.of("CHEESE");
    private static final List<Boolean> TEST_EMPTY_LIST = new LinkedList<>();

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL);
        Assertions.assertEquals(2, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("first"));
        Assertions.assertTrue(node.getOutputs().containsKey("rest"));
    }

    @Test
    public void correctlySeparatesInHeadMode() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var output = BasicNodes.LIST_HEADTAIL.compute(input, node.settings());
        Assertions.assertEquals(TEST_NUM_LIST.get(0), output.get("first").join().value());
        Assertions.assertEquals(TEST_NUM_LIST.subList(1, TEST_NUM_LIST.size()), output.get("rest").join().value());
    }

    @Test
    public void correctlySeparatesInTailMode() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL)
            .modifySetting("extraction_mode", ListHeadTailNodeType.getTailExtractionBox());
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_NUM_LIST)
        );
        var output = BasicNodes.LIST_HEADTAIL.compute(input, node.settings());
        Assertions.assertEquals(TEST_NUM_LIST.get(TEST_NUM_LIST.size() - 1), output.get("first").join().value());
        Assertions.assertEquals(TEST_NUM_LIST.subList(0, TEST_NUM_LIST.size() - 1), output.get("rest").join().value());
    }

    @Test
    public void correctlyExtractsSizeOneLists() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_STR_LIST)
        );
        var output = BasicNodes.LIST_HEADTAIL.compute(input, node.settings());
        Assertions.assertEquals(TEST_STR_LIST.get(0), output.get("first").join().value());
        Assertions.assertEquals(List.of(), output.get("rest").join().value());

        node.modifySetting("extraction_mode", ListHeadTailNodeType.getTailExtractionBox());
        var output1 = BasicNodes.LIST_HEADTAIL.compute(input, node.settings());
        Assertions.assertEquals(TEST_STR_LIST.get(0), output1.get("first").join().value());
        Assertions.assertEquals(List.of(), output1.get("rest").join().value());
    }

    @Test
    public void correctlyReturnsNullForEmptyLists() {
        var node = Node.create(BasicNodes.LIST_HEADTAIL);
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(TEST_EMPTY_LIST)
        );
        var output = BasicNodes.LIST_HEADTAIL.compute(input, node.settings());
        Assertions.assertEquals(Optional.empty(), output.get("first").join().value());
        Assertions.assertEquals(List.of(), output.get("rest").join().value());
    }
}
