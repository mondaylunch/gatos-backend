package club.mondaylunch.gatos.basicnodes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class HTTPRequestNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "url", DataType.STRING.create(""),
            "method", DataType.STRING.create("")
        );
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "body", DataType.STRING));
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "StatusCode", DataType.NUMBER),
            new NodeConnector.Output<>(nodeId, "responseText", DataType.STRING)
            );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var url = DataBox.get(settings, "url", DataType.STRING).orElse("");
        var method = DataBox.get(settings, "method", DataType.STRING).orElse("").toLowerCase();
        var body = DataBox.get(inputs, "body", DataType.STRING).orElse("");
        
        // not a valid request
        boolean notValid = url.equals("") || method.equals("");
        if (notValid) {
            return this.handleInvalidReturns();
        }

        Builder builder = HttpRequest.newBuilder();
        HttpRequest request;

        double statusCode;
        String responseBody;
        
        System.out.println(url + " " + method + " " + body);
        
        try {
            // TODO: extend this with an enum
            // creating the request
            switch (method) {
                case "get": request = builder.uri(new URI(url)).GET().build(); break;
                case "post": request = builder.uri(new URI(url)).POST(BodyPublishers.ofString(body)).build(); break;
                case "put": request = builder.uri(new URI(url)).PUT(BodyPublishers.ofString(body)).build(); break;
                case "delete": request = builder.uri(new URI(url)).DELETE().build(); break;
                default: return this.handleInvalidReturns();
            }
            
            // sending the request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            statusCode = response.statusCode();
            responseBody = response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            // if a problem happens just return nothing
            return this.handleInvalidReturns();
        }

        return Map.of(
            "StatusCode", CompletableFuture.completedFuture(DataType.NUMBER.create(statusCode)),
            "responseText", CompletableFuture.completedFuture(DataType.STRING.create(responseBody))
        );
    }

    private Map<String, CompletableFuture<DataBox<?>>> handleInvalidReturns() {
        return Map.of(
            "StatusCode", CompletableFuture.completedFuture(DataType.NUMBER.create(404.0)),
            "responseText", CompletableFuture.completedFuture(DataType.STRING.create("URL or method are incorrect"))
        );
    }
}
