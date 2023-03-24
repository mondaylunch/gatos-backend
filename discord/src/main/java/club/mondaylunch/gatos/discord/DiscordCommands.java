package club.mondaylunch.gatos.discord;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordCommands extends ListenerAdapter {

    private final Map<Long, Consumer<SlashCommandInteractionEvent>> commandHandlers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> commandsById = new ConcurrentHashMap<>();

    private final GatosDiscord gatosDiscord;

    public DiscordCommands(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        GatosDiscord.LOGGER.info("Received command: {}", event.getFullCommandName());
        if (event.getGuild() == null) {
            GatosDiscord.LOGGER.info("Ignoring command from DM");
            return;
        }
        var commandId = event.getCommandIdLong();
        var handler = this.commandHandlers.get(commandId);
        if (handler == null) {
            GatosDiscord.LOGGER.warn("No handler for command: {}", event.getFullCommandName());
        } else {
            GatosDiscord.LOGGER.info("Handling command: {} with flow", event.getFullCommandName());
            handler.accept(event);
        }
    }

    public void createSlashCommandListener(UUID id, String commandName, Guild guild, Consumer<SlashCommandInteractionEvent> function) {
        GatosDiscord.LOGGER.info("Creating command: {}", commandName);
        guild.upsertCommand(commandName, "A command powered by Gatos").queue(command -> {
            var commandId = command.getIdLong();
            this.commandHandlers.put(commandId, function);
            this.commandsById.put(id, commandId);
            GatosDiscord.LOGGER.info("Created command {} with id {}", commandName, commandId);
        });
    }

    public void removeSlashCommandListener(UUID id) {
        GatosDiscord.LOGGER.info("Removing command: {}", id);
        var commandId = this.commandsById.remove(id);
        if (commandId == null) {
            GatosDiscord.LOGGER.warn("Tried to remove nonexistent command with id: {}", id);
            return;
        }
        this.gatosDiscord.getJda()
            .deleteCommandById(commandId)
            .queue();
        this.commandHandlers.remove(commandId);
        GatosDiscord.LOGGER.info("Removed command {} with id {}", id, commandId);
    }
}
