package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ListMappingNodeTypeTest {
    private final DataType<?> inputType = DataType.STRING.listOf();
    private final DataType<?> outputType = DataType.NUMBER.listOf();
    private final NodeType.Process mappingNode = BasicNodes.STRING_LENGTH;
    private final Node node = Node.create(BasicNodes.LIST_MAPPING)
        .modifySetting("mapping_node", DataType.PROCESS_NODE_TYPE.create(this.mappingNode));

    @Test
    public void areInputsCorrect() {
        Assertions.assertEquals(Node.create(this.mappingNode).inputs().size(), this.node.inputs().size());
        var listInput = this.node.getInputWithName("list_input");
        Assertions.assertTrue(listInput.isPresent());
        Assertions.assertEquals(this.inputType, listInput.get().type());
    }

    @Test
    public void areOutputsCorrect() {
        Assertions.assertEquals(1, this.node.getOutputs().size());
        var mappedList = this.node.getOutputWithName("mapped_list");
        Assertions.assertTrue(mappedList.isPresent());
        Assertions.assertEquals(this.outputType, mappedList.get().type());
    }

    @Test
    public void areSettingsCorrect() {
        Assertions.assertEquals(3, BasicNodes.LIST_MAPPING.settings().size());
        Assertions.assertTrue(BasicNodes.LIST_MAPPING.settings().containsKey("mapping_node"));
        Assertions.assertTrue(BasicNodes.LIST_MAPPING.settings().containsKey("input_connector"));
        Assertions.assertTrue(BasicNodes.LIST_MAPPING.settings().containsKey("output_connector"));
    }

    @Test
    public void incorrectSettingsFailValidation() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.LIST_MAPPING);
        node = graph.modifyNode(node.id(), n -> n.modifySetting("mapping_node", DataType.PROCESS_NODE_TYPE.create(this.mappingNode)).modifySetting("input_connector", DataType.STRING.create("fgjklsdkg")));

        Assertions.assertTrue(node.type().isValid(node, graph).stream().anyMatch(e -> e.message().toLowerCase(Locale.ROOT).contains("input connector")));
        Assertions.assertFalse(node.type().isValid(node, graph).stream().anyMatch(e -> e.message().toLowerCase(Locale.ROOT).contains("output connector")));

        node = graph.modifyNode(node.id(), n -> n.modifySetting("input_connector", DataType.STRING.create("input")).modifySetting("output_connector", DataType.STRING.create("jksghklsgtres")));

        Assertions.assertFalse(node.type().isValid(node, graph).stream().anyMatch(e -> e.message().toLowerCase(Locale.ROOT).contains("input connector")));
        Assertions.assertTrue(node.type().isValid(node, graph).stream().anyMatch(e -> e.message().toLowerCase(Locale.ROOT).contains("output connector")));
    }

    @Test
    public void correctSettingsPassValidation() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.LIST_MAPPING);
        node = graph.modifyNode(node.id(), n -> n.modifySetting("mapping_node", DataType.PROCESS_NODE_TYPE.create(this.mappingNode))
            .modifySetting("input_connector", DataType.STRING.create("input"))
            .modifySetting("output_connector", DataType.STRING.create("output")));

        Assertions.assertFalse(node.type().isValid(node, graph).stream().anyMatch(e -> e.message().toLowerCase(Locale.ROOT).contains("connector not found on mapping node")));
    }

    @Test
    public void correctlyMapsList() {
        Map<String, DataBox<?>> inputs = Map.of("list_input", DataType.STRING.listOf().create(List.of("a", "bb", "ccc")));
        var results = BasicNodes.LIST_MAPPING.compute(UUID.randomUUID(), inputs, this.node.settings(), Map.of());
        var mappedList = results.get("mapped_list").join();
        Assertions.assertEquals(mappedList.type(), this.outputType);
        Assertions.assertTrue(mappedList.value() instanceof List<?>);
        Assertions.assertEquals(List.of(1.0, 2.0, 3.0), mappedList.value());
    }
}
