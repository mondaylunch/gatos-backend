package club.mondaylunch.gatos.basicnodes.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class HTTPRequestNodeTest {
    private static final String URL = "http://localhost:8000/test";

    @BeforeAll
    public static void startServer() {
        // create the server
        try {
            HttpMockServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void areInputsCorrect() {
        var node = Node.create(BasicNodes.HTTP_REQUEST);
        Assertions.assertEquals(1, node.inputs().size());
        Assertions.assertTrue(node.inputs().containsKey("body"));
    }

    @Test
    public void areOutputsCorrect() {
        var node = Node.create(BasicNodes.HTTP_REQUEST);
        Assertions.assertEquals(2, node.getOutputs().size());
        Assertions.assertTrue(node.getOutputs().containsKey("StatusCode"));
        Assertions.assertTrue(node.getOutputs().containsKey("responseText"));
    }

    @Test
    public void createGETRequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
        .modifySetting("url", DataType.STRING.create(URL))
        .modifySetting("method", DataType.STRING.create("GET"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("no query was sent :(", output.get("responseText").join().value());
    }

    @Test
    public void createPOSTRequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
        .modifySetting("url", DataType.STRING.create(URL))
        .modifySetting("method", DataType.STRING.create("POST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("asdsdf")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("no query was sent :(", output.get("responseText").join().value());
    }
}
