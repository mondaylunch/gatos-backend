package club.mondaylunch.gatos.discord;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class DiscordEvents implements EventListener {
    private final Map<UUID, EventListenerInfo<?>> eventListeners = new LinkedHashMap<>();

    public <T> void createEventListener(UUID id, Class<T> eventClass, Consumer<T> function) {
        GatosDiscord.LOGGER.info("Creating event listener: {}", id);
        this.eventListeners.put(id, new EventListenerInfo<>(eventClass, function));
    }

    public void removeEventListener(UUID id) {
        GatosDiscord.LOGGER.info("Removing event listener: {}", id);
        this.eventListeners.remove(id);
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        GatosDiscord.LOGGER.debug("Received event: {}", event);
        for (EventListenerInfo<?> info : this.eventListeners.values()) {
            info.maybeAccept(event);
        }
    }

    private record EventListenerInfo<T>(Class<T> eventClass, Consumer<T> function) {
        private void maybeAccept(GenericEvent event) {
            if (this.eventClass.isInstance(event)) {
                GatosDiscord.LOGGER.info("Handling event: {}", event);
                this.function.accept(this.eventClass.cast(event));
            }
        }
    }
}
