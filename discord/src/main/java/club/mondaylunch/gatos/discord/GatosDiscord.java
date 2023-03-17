package club.mondaylunch.gatos.discord;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.models.User;

public class GatosDiscord implements GatosPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gatos Discord");
    private JDA jda;
    private DiscordCommands commands;
    private DiscordEvents events;
    private DiscordNodeTypes nodeTypes;

    @Override
    public void init() {
        if (this.jda != null) {
            LOGGER.info("Shutting down previous JDA instance");
            this.jda.shutdown();
        }

        this.commands = new DiscordCommands(this);

        try {
            LOGGER.info("Constructing new JDA instance");
            this.jda = JDABuilder.createDefault(this.getToken())
                .setActivity(Activity.competing("SEG"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(this.commands, this.events)
                .build()
                .awaitReady();
            LOGGER.info("JDA ready");
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to set up JDA", e);
        }

        this.jda.getGuilds().forEach(g -> g.updateCommands().queue());

        DiscordDataTypes.init();
        this.nodeTypes = new DiscordNodeTypes(this);
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

    public DiscordNodeTypes getNodeTypes() {
        return this.nodeTypes;
    }

    public boolean userHasAdminPermission(User user, Guild guild) {
        String id = user.getDiscordId();
        if (id == null) {
            return false;
        }
        Member member = guild.getMember(UserSnowflake.fromId(id));
        return member != null && member.hasPermission(Permission.ADMINISTRATOR);
    }

    public void createSlashCommandListener(UUID id, String commandName, Guild guild, Consumer<@Nullable SlashCommandInteractionEvent> function) {
        this.commands.createSlashCommandListener(id, commandName, guild, function);
    }

    public void removeSlashCommandListener(UUID id) {
        this.commands.removeSlashCommandListener(id);
    }

    public <T> void createEventListener(UUID id, Class<T> eventClass, Consumer<@Nullable T> function) {
        this.events.createEventListener(id, eventClass, function);
    }

    public void removeEventListener(UUID id) {
        this.events.removeEventListener(id);
    }
}
