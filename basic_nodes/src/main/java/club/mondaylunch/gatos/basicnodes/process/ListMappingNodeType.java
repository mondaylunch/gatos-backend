package club.mondaylunch.gatos.basicnodes.process;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;

public class ListMappingNodeType extends NodeType.Process {

    private static final String MAPPING_NODE_SETTING = "mapping_node";
    private static final String INPUT_CONNECTOR_SETTING = "input_connector";
    private static final String OUTPUT_CONNECTOR_SETTING = "output_connector";
    private static final String LIST_INPUT = "list_input";
    private static final String MAPPED_LIST_OUTPUT = "mapped_list";

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            MAPPING_NODE_SETTING, DataType.PROCESS_NODE_TYPE.create(BasicNodes.TRUTHINESS),
            INPUT_CONNECTOR_SETTING, DataType.STRING.create("input"),
            OUTPUT_CONNECTOR_SETTING, DataType.STRING.create("output")
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> graph) {
        Set<GraphValidityError> errors = new HashSet<>(super.isValid(node, graph));
        var inputConnectorName = DataBox.get(node.settings(), INPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var outputConnectorName = DataBox.get(node.settings(), OUTPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var mappingNode = getMappingNode(node.settings());
        var inputConnector = mappingNode.getInputWithName(inputConnectorName);
        var outputConnector = mappingNode.getOutputWithName(outputConnectorName);
        if (inputConnector.isEmpty()) {
            errors.add(new GraphValidityError(node.id(), "Input connector not found on mapping node"));
        }
        if (outputConnector.isEmpty()) {
            errors.add(new GraphValidityError(node.id(), "Output connector not found on mapping node"));
        }
        return errors;
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputConnectorName = DataBox.get(settings, INPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var mappingNode = getMappingNode(settings);
        var inputConnector = mappingNode.getInputWithName(inputConnectorName);
        if (inputConnector.isEmpty()) {
            return Set.of(
                new Input<>(nodeId, LIST_INPUT, ListDataType.GENERIC_LIST)
            );
        }

        var otherInputs = mappingNode.inputs().values().stream()
            .filter(input -> !input.equals(inputConnector.get()))
            .toList();

        var listInputType = getListType(inputConnector.get().type());

        Set<Input<?>> inputs = new HashSet<>();
        inputs.add(new Input<>(nodeId, LIST_INPUT, listInputType));
        for (var otherInput : otherInputs) {
            inputs.add(new Input<>(nodeId, otherInput.name(), otherInput.type()));
        }
        return inputs;
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var outputConnectorName = DataBox.get(settings, OUTPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var mappingNode = getMappingNode(settings);
        var outputConnector = mappingNode.getOutputWithName(outputConnectorName);
        if (outputConnector.isEmpty()) {
            return Set.of(
                new Output<>(nodeId, MAPPED_LIST_OUTPUT, ListDataType.GENERIC_LIST)
            );
        }

        var otherOutputs = mappingNode.outputs().values().stream()
            .filter(output -> !output.equals(outputConnector.get()))
            .toList();

        var listOutputType = getListType(outputConnector.get().type());

        Set<Output<?>> outputs = new HashSet<>();
        outputs.add(new Output<>(nodeId, MAPPED_LIST_OUTPUT, listOutputType));
        for (var otherOutput : otherOutputs) {
            outputs.add(new Output<>(nodeId, otherOutput.name(), otherOutput.type()));
        }

        return outputs;
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputConnectorName = DataBox.get(settings, INPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var outputConnectorName = DataBox.get(settings, OUTPUT_CONNECTOR_SETTING, DataType.STRING).orElseThrow();
        var mappingNode = getMappingNode(settings);
        var inputConnector = mappingNode.getInputWithName(inputConnectorName).orElseThrow();
        var outputConnector = mappingNode.getOutputWithName(outputConnectorName).orElseThrow();
        DataType<List<Object>> listOutputType = getListType(outputConnector.type());

        var inputListBox = inputs.get(LIST_INPUT);
        var inputListType = inputListBox.type();
        DataType<?> contentsType = inputListType instanceof ListDataType<?> listDataType ? listDataType.contains() : DataType.ANY;
        List<CompletableFuture<Object>> resultList = mapInput(userId, (List<?>) inputListBox.value(), mappingNode, inputs, inputConnector, outputConnector, contentsType, (NodeType.Process) mappingNode.type());
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(resultList.toArray(CompletableFuture[]::new));
        return Map.of(MAPPED_LIST_OUTPUT, resultFuture.thenApply(v -> listOutputType.create(resultList.stream().map(CompletableFuture::join).toList())));
    }

    @SuppressWarnings("unchecked")
    private static DataType<List<Object>> getListType(DataType<?> type) {
        return (DataType<List<Object>>) (type == DataType.ANY ? ListDataType.GENERIC_LIST : type.listOf());
    }

    @SuppressWarnings("unchecked")
    private static <E1, E2> List<CompletableFuture<E2>> mapInput(
        UUID flowId,
        List<?> inputList,
        Node mappingNode,
        Map<String, DataBox<?>> inputs,
        Input<?> inputConnector,
        Output<?> outputConnector,
        DataType<?> contentsType,
        NodeType.Process mappingNodeType
    ) {
        return ((List<E1>) inputList).stream().map(element -> (CompletableFuture<E2>) mapSingleElement(
            flowId,
            element,
            mappingNode,
            inputs,
            inputConnector,
            outputConnector,
            (DataType<E1>) contentsType,
            mappingNodeType
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private static <E1, E2> CompletableFuture<E2> mapSingleElement(
        UUID flowId,
        E1 element,
        Node mappingNode,
        Map<String, DataBox<?>> inputs,
        Input<?> inputConnector,
        Output<?> outputConnector,
        DataType<E1> contentsType,
        NodeType.Process mappingNodeType
    ) {
        var mappingNodeInputs = new HashMap<String, DataBox<?>>();
        for (var input : mappingNode.inputs().values()) {
            if (input.equals(inputConnector)) {
                mappingNodeInputs.put(input.name(), contentsType.create(element));
            } else {
                mappingNodeInputs.put(input.name(), inputs.get(input.name()));
            }
        }
        var results = mappingNodeType.compute(flowId, mappingNodeInputs, mappingNode.settings(), mappingNode.inputTypes());
        return (CompletableFuture<E2>) results.get(outputConnector.name()).thenApply(DataBox::value);
    }

    private static Node getMappingNode(Map<String, DataBox<?>> settings) {
        return Node.create(DataBox.get(settings, MAPPING_NODE_SETTING, DataType.PROCESS_NODE_TYPE).orElseThrow());
    }
}
