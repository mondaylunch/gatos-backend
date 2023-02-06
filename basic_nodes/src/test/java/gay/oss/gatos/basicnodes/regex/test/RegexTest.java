package gay.oss.gatos.basicnodes.regex.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.executor.GraphExecutor;
import gay.oss.gatos.core.graph.Graph;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.graph.type.NodeType;
import gay.oss.gatos.basicnodes.regex.RegexMatchesNodeType;

public class RegexTest {
    private static final NodeType TEST_REGEX_MATCHES_NODE_TYPE = new RegexMatchesNodeType();

    @Test   
    public void nodeAddsToGraph() {
        var graph = new Graph();
        var node = graph.addNode(TEST_REGEX_MATCHES_NODE_TYPE);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void matchesSingleCharToSingleChar() {
        var graph = new Graph();
        var node = graph.addNode(TEST_REGEX_MATCHES_NODE_TYPE);
        graph.modifyNode(node.id(), n -> n.modifySetting("regex", DataType.STRING.create("a")));
        graph.modifyNode(node.id(), n -> n.modifySetting("word" , DataType.STRING.create("a")));

        var result = new AtomicBoolean();
        var ouput = graph.addNode(new OutputBoolNodeType(result));

        connectBool(graph, node, "ouput", output, "in");
        
        var executionOrder = graph.getExecutionOrder();
        Assertions.assertTrue(executionOrder.isPresent());
        var executor = new GraphExecutor(executionOrder.get(), graph.getConnections());

        CompletableFuture.runAsync(executor.execute()).join();
        Assertions.assertTrue(result.get());
    }

    @Test
    public void matchesWordToWord() {
        var graph1 = new Graph();
        var node1 = graph1.addNode(TEST_REGEX_MATCHES_NODE_TYPE);
        graph1.modifyNode(node1.id(), n -> n.modifySetting("regex", DataType.STRING.create("^https?://")));
        graph1.modifyNode(node1.id(), n -> n.modifySetting("word" , DataType.STRING.create("https://my.website.co.uk")));

        var result1 = new AtomicBoolean();
        var ouput1 = graph1.addNode(new OutputBoolNode1Type(result1));

        connectBool(graph1, node1, "ouput", output1, "in");
        
        var graph2 = new Graph();
        var node2 = graph2.addNode(TEST_REGEX_MATCHES_NODE_TYPE);
        graph2.modifyNode(node2.id(), n -> n.modifySetting("regex", DataType.STRING.create("^https?://")));
        graph2.modifyNode(node2.id(), n -> n.modifySetting("word" , DataType.STRING.create("this is totally a website link")));

        var result2 = new AtomicBoolean();
        var ouput2 = graph1.addNode(new OutputBoolNode1Type(result2));

        connectBool(graph2, node2, "ouput", output2, "in");        

        var executionOrder1 = graph1.getExecutionOrder();
        Assertions.assertTrue(executionOrder1.isPresent());

        var executionOrder2 = graph2.getExecutionOrder();
        Assertions.assertTrue(executionOrder2.isPresent());
        
        var executor1 = new GraphExecutor(executionOrder1.get(), graph1.getConnections());
        var executor2 = new GraphExecutor(executionOrder2.get(), graph2.getConnections());

        CompletableFuture.runAsync(executor1.execute()).join();
        CompletableFuture.runAsync(executor2.execute()).join();
        Assertions.assertTrue(result1.get());
        Assertions.assertFalse(result2.get());
    }

    private static final class OutputBoolNodeType extends NodeType.End {
        public final AtomicBoolean result;

        private OutputBoolNodeType(AtomicBoolean result) {
            this.result = result;
        }

        @Override
        public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
            return Set.of(
                new NodeConnector.Input<>(nodeId, "in", DataType.BOOLEAN)
            );
        }

        @Override
        public Map<String, DataBox<?>> settings() {
            return Map.of();
        }

        @Override
        public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
            this.result.set(DataBox.get(inputs, "in", DataType.BOOLEAN).orElseThrow());
            return CompletableFuture.runAsync(() -> {});
        }
    }

    private static void connectString(Graph graph, Node a, String connectorA, Node b, String connectorB) {
        var conn = NodeConnection.createConnection(
            a, connectorA,
            b, connectorB,
            DataType.STRING
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
    }

    private static void connectBool(Graph graph, Node a, String connectorA, Node b, String connectorB) {
        var conn = NodeConnection.createConnection(
            a, connectorA,
            b, connectorB,
            DataType.BOOLEAN
        );
        Assertions.assertTrue(conn.isPresent());
        graph.addConnection(conn.get());
    }


}
