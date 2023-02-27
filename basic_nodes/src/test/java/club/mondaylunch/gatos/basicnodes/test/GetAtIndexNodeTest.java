package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;

public class GetAtIndexNodeTest {

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(2, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("input"));
        Assertions.assertTrue(node.inputs().containsKey("index"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.GET_AT_INDEX);
        Assertions.assertEquals(1, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("output"));
    }

    @Test
    public void correctlyEvaluatesEmptyLists() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(0.0)
        );
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesIndexOutOfRange() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(6.0)
        );
        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, output.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesLists() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataBox<?>> input2 = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(1.0)
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), Map.of());
        Assertions.assertEquals(0, output.get("output").join().value());
        Assertions.assertEquals(output.get("output").join().type(), DataType.ANY);

        var output2 = BasicNodes.GET_AT_INDEX.compute(input2, Map.of(), Map.of());
        Assertions.assertEquals(1, output2.get("output").join().value());
    }

    @Test
    public void correctlyEvaluatesReturnType() {
        List<Integer> testArrayList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            testArrayList.add(i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testArrayList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.NUMBER
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(0, output.get("output").join().value());
        Assertions.assertEquals(output.get("output").join().type(), DataType.NUMBER);
    }

    @Test
    public void correctlyEvaluatesReturnTypeWithWrongInput() {
        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(new ArrayList<>()),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.NUMBER
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals(OptionalDataType.GENERIC_OPTIONAL, output.get("output").join().value());
        Assertions.assertEquals(output.get("output").join().type(), DataType.NUMBER);
    }

    @Test
    public void correctlyEvaluatesLinkedLists() {
        List<String> testLinkedList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            testLinkedList.add("i" + i);
        }

        Map<String, DataBox<?>> input = Map.of(
            "input", ListDataType.GENERIC_LIST.create(testLinkedList),
            "index", DataType.NUMBER.create(0.0)
        );

        Map<String, DataType<?>> inputTypes = Map.of(
            "input", DataType.STRING
        );

        var output = BasicNodes.GET_AT_INDEX.compute(input, Map.of(), inputTypes);
        Assertions.assertEquals("i0", output.get("output").join().value());
        Assertions.assertEquals(output.get("output").join().type(), DataType.STRING);
    }

    @Test
    public void correctlyEvaluatesLinkedNodesInGraph() {
        List<Node> nodeList = new ArrayList<>();
        var firstNode = Node.create(BasicNodes.GET_AT_INDEX);
        var secondNode = Node.create(BasicNodes.GET_AT_INDEX);
        nodeList.add(firstNode);
        nodeList.add(secondNode);

        List<NodeConnection<?>> connectionsList = new ArrayList<>();
        var connection = NodeConnection.createConnection(firstNode, "output", secondNode, "input", DataType.NUMBER);
        connectionsList.add(connection.get());
        Graph graph = new Graph(nodeList, Map.of(), connectionsList);
        Assertions.assertEquals(graph.getNode(secondNode.id()).get().getOutputWithName("output"), DataType.NUMBER);
    }

}
