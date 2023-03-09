package club.mondaylunch.gatos.core.graph;

import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public record WebhookStartNodeInput(
    @Nullable JsonObject requestBody,
    AtomicReference<?> endOutput
) {
}
