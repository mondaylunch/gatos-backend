package club.mondaylunch.gatos.basicnodes.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.basicnodes.ListSetOperationNodeType;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListSetOperationNodeTest {
    private static final List<Double> TEST_NUM_LIST_1 = Arrays.asList(2., 4., 6., 8., 3.5, Math.PI, Math.PI);
    private static final List<Double> TEST_NUM_LIST_2 = Arrays.asList(Math.sqrt(2), Math.PI, 3.5, 3.5, 6.);
    private static final List<String> TEST_STR_LIST_1 = Arrays.asList("n", "e", "r", "d");
    private static final String FIRST_KEY = "list_first";
    private static final String SECOND_KEY = "list_second";
    private static final String SETTING_KEY = "set_operation";

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey(FIRST_KEY));
        Assertions.assertTrue(node.inputs().containsKey(SECOND_KEY));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyComputesUnion() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION)
            .modifySetting(SETTING_KEY, ListSetOperationNodeType.getOperationSettingDataBox("union"));
        Map<String, DataBox<?>> dataInputs = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2)
        );
        Map<String, DataBox<?>> dataInputs1 = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            FIRST_KEY, DataType.NUMBER.listOf(),
            SECOND_KEY, DataType.NUMBER.listOf()
        );
        var output = BasicNodes.LIST_SET_OPERATION.compute(dataInputs, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(2., 4., 6., 8., 3.5, Math.PI, Math.sqrt(2)), output.get("output").join().value());

        var output1 = BasicNodes.LIST_SET_OPERATION.compute(dataInputs1, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(Math.sqrt(2), Math.PI, 3.5, 6., 2., 4., 8.), output1.get("output").join().value());

        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> ((List<Double>) output1.get("output").join().value()).add(Double.NaN)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyComputesIntersections() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION)
            .modifySetting(SETTING_KEY, ListSetOperationNodeType.getOperationSettingDataBox("intersection"));
        Map<String, DataBox<?>> dataInputs = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2)
        );
        Map<String, DataBox<?>> dataInputs1 = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            FIRST_KEY, DataType.NUMBER.listOf(),
            SECOND_KEY, DataType.NUMBER.listOf()
        );
        var output = BasicNodes.LIST_SET_OPERATION.compute(dataInputs, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(6., 3.5, Math.PI), output.get("output").join().value());

        var output1 = BasicNodes.LIST_SET_OPERATION.compute(dataInputs1, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(Math.PI, 3.5, 6.), output1.get("output").join().value());

        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> ((List<Double>) output1.get("output").join().value()).add(Double.NaN)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correctlyComputesDifference() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION)
            .modifySetting(SETTING_KEY, ListSetOperationNodeType.getOperationSettingDataBox("difference"));
        Map<String, DataBox<?>> dataInputs = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2)
        );
        Map<String, DataBox<?>> dataInputs1 = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_2),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            FIRST_KEY, DataType.NUMBER.listOf(),
            SECOND_KEY, DataType.NUMBER.listOf()
        );
        var output = BasicNodes.LIST_SET_OPERATION.compute(dataInputs, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(2., 4., 8.), output.get("output").join().value());

        var output1 = BasicNodes.LIST_SET_OPERATION.compute(dataInputs1, node.settings(), typeInputs);
        Assertions.assertEquals(List.of(Math.sqrt(2)), output1.get("output").join().value());

        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> ((List<Double>) output1.get("output").join().value()).add(Double.NaN)
        );
    }

    @Test
    public void doesntWorkWithDifferentListTypes() {
        var node = Node.create(BasicNodes.LIST_SET_OPERATION)
            .modifySetting(SETTING_KEY, ListSetOperationNodeType.getOperationSettingDataBox("union"));
        Map<String, DataBox<?>> dataInputs = Map.of(
            FIRST_KEY, ListDataType.GENERIC_LIST.create(TEST_NUM_LIST_1),
            SECOND_KEY, ListDataType.GENERIC_LIST.create(TEST_STR_LIST_1)
        );
        Map<String, DataType<?>> typeInputs = Map.of(
            FIRST_KEY, DataType.NUMBER.listOf(),
            SECOND_KEY, DataType.STRING.listOf()
        );
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> BasicNodes.LIST_SET_OPERATION.compute(dataInputs, node.settings(), typeInputs)
        );
    }
}
