package club.mondaylunch.gatos.core.graph;

import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;

public record WebhookStartNodeInput(
    JsonObject requestBody,
    AtomicReference<?> endOutput
) {
}
