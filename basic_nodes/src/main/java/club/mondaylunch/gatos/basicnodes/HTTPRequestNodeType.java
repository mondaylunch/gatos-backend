package club.mondaylunch.gatos.basicnodes;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
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
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var url = DataBox.get(settings, "url", DataType.STRING).orElse("");
        var method = DataBox.get(settings, "method", DataType.STRING).orElse("").toUpperCase();
        var body = DataBox.get(inputs, "body", DataType.STRING).orElse("");
        
        // get uri by checking url and method
        URI uri = this.getURI(url, method);
        if (uri == null) {
            return this.handleInvalidReturns();
        }

        // the url and method are valid so we create and send the request
        HttpRequest request = this.createRequest(method, uri, body);
        HttpClient httpClient = HttpClient.newHttpClient();

        CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, BodyHandlers.ofString()).exceptionally(ex -> null);
        return Map.of(
            "StatusCode", future.thenApply(response -> response == null ? DataType.NUMBER.create(404.0) : DataType.NUMBER.create((double) response.statusCode())),
            "responseText", future.thenApply(response -> response == null ? DataType.STRING.create("URL or method are incorrect") : DataType.STRING.create(response.body()))
        );
    }

    private Map<String, CompletableFuture<DataBox<?>>> handleInvalidReturns() {
        return Map.of(
            "StatusCode", CompletableFuture.completedFuture(DataType.NUMBER.create(404.0)),
            "responseText", CompletableFuture.completedFuture(DataType.STRING.create("URL or method are incorrect"))
        );
    }

    private URI getURI(String url, String method) {
        URI uri = null;
        if (this.validateMethod(method)) {
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                uri = null;
            }
        }
        return uri;
    }

    private boolean validateMethod(String method) {
        return Methods.isMethod(method);
    }

    private HttpRequest createRequest(String method, URI uri, String body) {
        // note: this method is called after validating the method parameter so the switch will execute
        // no null will be returned 
        Builder builder = HttpRequest.newBuilder();
        return switch (method) {
            case "GET" -> builder.uri(uri).GET().build();
            case "POST" -> builder.uri(uri).POST(BodyPublishers.ofString(body)).build();
            case "PUT" -> builder.uri(uri).PUT(BodyPublishers.ofString(body)).build();
            case "DELETE" -> builder.uri(uri).DELETE().build();
            default -> null;
        };
    }

    private enum Methods {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        private final String method;
        Methods(String method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return this.method;
        }

        public static boolean isMethod(String method) {
            return List.of(GET.toString(), POST.toString(), PUT.toString(), DELETE.toString()).contains(method);
        }
    }
}
