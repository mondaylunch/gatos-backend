package club.mondaylunch.gatos.discord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;

public class GatosCommands implements EventListener {

    private final Map<Command, Consumer<SlashCommandInteractionEvent>> commandHandlers = new HashMap<net.dv8tion.jda.api.interactions.commands.Command, Consumer<SlashCommandInteractionEvent>>();
    private final Map<UUID, Command> commandsById = new HashMap<>();

    private final GatosDiscord gatosDiscord;

    public GatosCommands(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof SlashCommandInteractionEvent slash && slash.getGuild() != null) {
            slash.getInteraction().getFullCommandName();
            var handler = this.commandHandlers.get(slash.getGuild().retrieveCommandById(slash.getCommandId()).complete());
            if (handler == null) {
                GatosDiscord.LOGGER.warn("No handler for command: " + slash.getInteraction().getFullCommandName());
            } else {
                handler.accept(slash);
            }
        }
    }

    public void createSlashCommandListener(UUID id, String commandName, Guild guild, Consumer<SlashCommandInteractionEvent> function) {
        var cmd = guild.upsertCommand(commandName, "A command powered by Gatos").complete();
        this.commandHandlers.put(cmd, function);
        this.commandsById.put(id, cmd);
    }

    public void removeSlashCommandListener(UUID id) {
        var cmd = this.commandsById.remove(id);
        if (cmd != null) {
            cmd.delete().queue();
            this.commandHandlers.remove(cmd);
            this.commandsById.remove(id);
        }
    }
}
