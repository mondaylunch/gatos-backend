package club.mondaylunch.gatos.basicnodes.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.parameters.P;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HTTPRequestNodeTest {
    
    private static final String CONTEXT = "/app";
    private static final int PORT = 8000;
    private static final String URL = "http://localhost:8000/app";

    // @BeforeEach
    // public void startServer() {
    //     // create the server
    //     HttpMockServer server = new HttpMockServer(PORT, CONTEXT, new HttpRequestHandler());
    //     server.start();
    //     System.out.println("started server on port: " + PORT);
    // }

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
        HttpMockServer server = new HttpMockServer(PORT, CONTEXT, new HttpRequestHandler());
        server.start();
        System.out.println("started server on port: " + PORT);
        
        var node = Node.create(BasicNodes.HTTP_REQUEST)
                        .modifySetting("url", DataType.STRING.create(URL))
                        .modifySetting("method", DataType.STRING.create("GET"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(input, node.settings(), Map.of());
        Assertions.assertEquals("no query was sent :(", output.get("output").join().value());
    }

    @Test
    public void createPOSTRequest() {
        var node = Node.create(BasicNodes.HTTP_REQUEST)
                        .modifySetting("url", DataType.STRING.create(URL))
                        .modifySetting("method", DataType.STRING.create("POST"));

        Map<String, DataBox<?>> input = Map.of(
            "body", DataType.STRING.create("this is a request")
        );

        var output = BasicNodes.HTTP_REQUEST.compute(input, node.settings(), Map.of());
        Assertions.assertEquals("no query was sent :(", output.get("output").join().value());
    }

    public class HttpMockServer {
        private HttpServer httpServer;

        public HttpMockServer(int port, String context, HttpHandler handler) {
            try {
                this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
                this.httpServer.createContext(context, handler);
                this.httpServer.setExecutor(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            this.httpServer.start();
        }
    }

    public class HttpRequestHandler implements HttpHandler {
        private static final int HTTP_OK_STATUS = 200;

        @Override
        public void handle(HttpExchange t) throws IOException {
            URI uri = t.getRequestURI();

            String response = this.createResponse(uri);
            t.sendResponseHeaders(HTTP_OK_STATUS, response.getBytes().length);
            // potentially write them out
        }

        private String createResponse(URI uri) {
            String query = uri.getQuery();
            if ( query != null) {
                return "there was a query sent: " + query;
            }
            return "no query was sent :(";
        }
    }
}
