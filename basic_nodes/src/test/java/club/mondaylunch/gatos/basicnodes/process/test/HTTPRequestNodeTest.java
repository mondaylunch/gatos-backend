package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

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

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("GET request", output.get("responseText").join().value());
    }

    @Test
    public void createPOSTRequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create(URL))
            .modifySetting("method", DataType.STRING.create("POST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("asdsdf")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("POST request request has a body: asdsdf", output.get("responseText").join().value());
    }

    @Test
    public void canHandlePOSTWithoutBody() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create(URL))
            .modifySetting("method", DataType.STRING.create("POST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("POST request", output.get("responseText").join().value());
    }

    @Test
    public void createPUTRequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create(URL))
            .modifySetting("method", DataType.STRING.create("PUT"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("asdsdf")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("PUT request request has a body: asdsdf", output.get("responseText").join().value());
    }

    @Test
    public void createDELETERequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create(URL))
            .modifySetting("method", DataType.STRING.create("DELETE"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(200.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("DELETE request", output.get("responseText").join().value());
    }

    @Test
    public void handleIncorrectAPIs() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create("http://notlocalhost:8080/not_correct"))
            .modifySetting("method", DataType.STRING.create("POST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(404.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("URL or method are incorrect", output.get("responseText").join().value());
    }

    @Test
    public void handleIncorrectMethods() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
            .modifySetting("url", DataType.STRING.create(URL))
            .modifySetting("method", DataType.STRING.create("NOTPOST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(UUID.randomUUID(), input, node.settings(), Map.of());
        Assertions.assertEquals(404.0, output.get("StatusCode").join().value());
        Assertions.assertEquals("URL or method are incorrect", output.get("responseText").join().value());
    }
}
