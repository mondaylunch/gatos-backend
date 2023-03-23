package club.mondaylunch.gatos.core.graph;

import com.google.gson.JsonObject;

import club.mondaylunch.gatos.core.models.JsonObjectReference;

public record WebhookStartNodeInput(
    JsonObject requestBody,
    JsonObjectReference endOutput
) {
}
