package club.mondaylunch.gatos.basicnodes.start;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.WebhookStartNodeInput;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.JsonObjectReference;

public class WebhookStartNodeType extends NodeType.Start<WebhookStartNodeInput> {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "requestBody", DataType.JSON_OBJECT),
            new NodeConnector.Output<>(nodeId, "endOutputReference", DataType.REFERENCE)
        );
    }

    @Override
    public void setupFlow(Flow flow, Consumer<@Nullable WebhookStartNodeInput> function, Node node) {
    }

    @Override
    public void teardownFlow(Flow flow, Node node) {

    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, @Nullable WebhookStartNodeInput input, Map<String, DataBox<?>> settings) {
        if (input == null) {
            input = new WebhookStartNodeInput(new JsonObject(), new JsonObjectReference());
        }
        return Map.of(
            "requestBody", CompletableFuture.completedFuture(DataType.JSON_OBJECT.create(
                input.requestBody()
            )),
            "endOutputReference", CompletableFuture.completedFuture(DataType.REFERENCE.create(
                input.endOutput()
            ))
        );
    }
}
