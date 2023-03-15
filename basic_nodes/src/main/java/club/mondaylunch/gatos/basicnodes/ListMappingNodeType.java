package club.mondaylunch.gatos.basicnodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ListMappingNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "mapping_node", DataType.PROCESS_NODE_TYPE.create(BasicNodes.TRUTHINESS),
            "input_connector", DataType.STRING.create("input"),
            "output_connector", DataType.STRING.create("output")
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Graph graph) {
        Set<GraphValidityError> errors = new HashSet<>(super.isValid(node, graph));
        var mappingNodeType = DataBox.get(node.settings(), "mapping_node", DataType.PROCESS_NODE_TYPE).orElseThrow();
        var inputConnectorName = DataBox.get(node.settings(), "input_connector", DataType.STRING).orElseThrow();
        var outputConnectorName = DataBox.get(node.settings(), "output_connector", DataType.STRING).orElseThrow();
        var mappingNode = Node.create(mappingNodeType);
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
        var mappingNodeType = DataBox.get(settings, "mapping_node", DataType.PROCESS_NODE_TYPE).orElseThrow();
        var inputConnectorName = DataBox.get(settings, "input_connector", DataType.STRING).orElseThrow();
        var mappingNode = Node.create(mappingNodeType);
        var inputConnector = mappingNode.getInputWithName(inputConnectorName);
        if (inputConnector.isEmpty()) {
            return Set.of(
                new Input<>(nodeId, "list_input", ListDataType.GENERIC_LIST)
            );
        }

        var otherInputs = mappingNode.inputs().values().stream()
            .filter(input -> !input.equals(inputConnector.get()))
            .toList();

        var inputConnectorType = inputConnector.get().type();
        var listInputType = inputConnectorType == DataType.ANY ? ListDataType.GENERIC_LIST : inputConnectorType.listOf();

        Set<Input<?>> inputs = new HashSet<>();
        inputs.add(new Input<>(nodeId, "list_input", listInputType));
        for (var otherInput : otherInputs) {
            inputs.add(new Input<>(nodeId, otherInput.name(), otherInput.type()));
        }
        return inputs;
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var mappingNodeType = DataBox.get(settings, "mapping_node", DataType.PROCESS_NODE_TYPE).orElseThrow();
        var outputConnectorName = DataBox.get(settings, "output_connector", DataType.STRING).orElseThrow();
        var mappingNode = Node.create(mappingNodeType);
        var outputConnector = mappingNode.getOutputWithName(outputConnectorName);
        if (outputConnector.isEmpty()) {
            return Set.of(
                new Output<>(nodeId, "mapped_list", ListDataType.GENERIC_LIST)
            );
        }

        var otherOutputs = mappingNode.getOutputs().values().stream()
            .filter(output -> !output.equals(outputConnector.get()))
            .toList();

        var outputConnectorType = outputConnector.get().type();
        var listOutputType = outputConnectorType == DataType.ANY ? ListDataType.GENERIC_LIST : outputConnectorType.listOf();

        Set<Output<?>> outputs = new HashSet<>();
        outputs.add(new Output<>(nodeId, "mapped_list", listOutputType));
        for (var otherOutput : otherOutputs) {
            outputs.add(new Output<>(nodeId, otherOutput.name(), otherOutput.type()));
        }

        return outputs;
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var mappingNodeType = DataBox.get(settings, "mapping_node", DataType.PROCESS_NODE_TYPE).orElseThrow();
        var inputConnectorName = DataBox.get(settings, "input_connector", DataType.STRING).orElseThrow();
        var outputConnectorName = DataBox.get(settings, "output_connector", DataType.STRING).orElseThrow();
        var mappingNode = Node.create(mappingNodeType);
        var inputConnector = mappingNode.getInputWithName(inputConnectorName).orElseThrow();
        var outputConnector = mappingNode.getOutputWithName(outputConnectorName).orElseThrow();
        DataType<List<Object>> listOutputType = (DataType<List<Object>>) (outputConnector.type() == DataType.ANY ? ListDataType.GENERIC_LIST : outputConnector.type().listOf());

        var inputListBox = inputs.get("list_input");
        var inputListType = inputListBox.type();
        DataType<?> contentsType = inputListType instanceof ListDataType<?> listDataType ? listDataType.contains() : DataType.ANY;
        List<CompletableFuture<Object>> resultList = this.mapInput((List<?>) inputListBox.value(), mappingNode, inputs, inputConnector, outputConnector, contentsType, mappingNodeType);
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(resultList.toArray(CompletableFuture[]::new));
        return Map.of("mapped_list", resultFuture.thenApply(v -> listOutputType.create(resultList.stream().map(CompletableFuture::join).toList())));
    }

    @SuppressWarnings("unchecked")
    private <E1, E2> List<CompletableFuture<E2>> mapInput(List<?> inputList, Node mappingNode, Map<String, DataBox<?>> inputs, Input<?> inputConnector, Output<?> outputConnector, DataType<?> contentsType, NodeType.Process mappingNodeType) {
        return ((List<E1>) inputList).stream().map(element -> (CompletableFuture<E2>) this.mapSingleElement(element, mappingNode, inputs, inputConnector, outputConnector, (DataType<E1>) contentsType, mappingNodeType)).toList();
    }

    @SuppressWarnings("unchecked")
    private <E1, E2> CompletableFuture<E2> mapSingleElement(E1 element,
                                                            Node mappingNode,
                                                            Map<String, DataBox<?>> inputs,
                                                            Input<?> inputConnector,
                                                            Output<?> outputConnector,
                                                            DataType<E1> contentsType,
                                                            NodeType.Process mappingNodeType) {
        var mappingNodeInputs = new HashMap<String, DataBox<?>>();
        for (var input : mappingNode.inputs().values()) {
            if (input.equals(inputConnector)) {
                mappingNodeInputs.put(input.name(), contentsType.create(element));
            } else {
                mappingNodeInputs.put(input.name(), inputs.get(input.name()));
            }
        }
        var results = mappingNodeType.compute(mappingNodeInputs, mappingNode.settings(), mappingNode.inputTypes());
        return (CompletableFuture<E2>) results.get(outputConnector.name()).thenApply(DataBox::value);
    }
}
