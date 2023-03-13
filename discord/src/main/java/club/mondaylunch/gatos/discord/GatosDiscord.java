package club.mondaylunch.gatos.discord;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class GatosDiscord implements GatosPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gatos Discord");
    private JDA jda;
    private GatosCommands commands;

    @Override
    public void init() {
        if (this.jda != null) {
            LOGGER.info("Shutting down previous JDA instance");
            this.jda.shutdown();
        }

        this.commands = new GatosCommands(this);

        try {
            LOGGER.info("Constructing new JDA instance");
            this.jda = JDABuilder.createDefault(this.getToken())
                .setActivity(Activity.competing("SEG"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(this.commands)
                .build()
                .awaitReady();
            LOGGER.info("JDA ready");
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to set up JDA", e);
        }

        this.jda.getGuilds().forEach(g -> g.updateCommands().queue());

        DiscordDataTypes.init();
        NodeType.REGISTRY.register("discord.send_message", new SendDiscordMessageNode(() -> this.jda));
        NodeType.REGISTRY.register("discord.command", new DiscordCommandNodeType(this));
        NodeType.REGISTRY.register("discord.reply_to_command", new DiscordCommandReplyNodeType(this));
    }

    private String getToken() {
        var properties = new Properties();
        try (var stream = this.getClass().getResourceAsStream("/gatos_discord.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failure loading discord token", e);
        }
        return properties.getProperty("DISCORD_TOKEN");
    }

    public JDA getJda() {
        return this.jda;
    }

    public void createSlashCommandListener(UUID id, String commandName, Guild guild, Consumer<@Nullable SlashCommandInteractionEvent> function) {
        this.commands.createSlashCommandListener(id, commandName, guild, function);
    }

    public void removeSlashCommandListener(UUID id) {
        this.commands.removeSlashCommandListener(id);
    }
}
