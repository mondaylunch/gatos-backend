package club.mondaylunch.gatos.discord;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.Environment;
import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.models.Flow;
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

        if (this.getToken().isBlank()) {
            LOGGER.warn("Not going to try and run Discord, no token!");
            return;
        }

        this.commands = new DiscordCommands(this);
        this.events = new DiscordEvents();

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

        DiscordDataTypes.init(this);
        this.nodeTypes = new DiscordNodeTypes(this);
    }

    @Override
    public String name() {
        return "discord";
    }

    private String getToken() {
        return Environment.getDiscordToken();
    }

    public JDA getJda() {
        return this.jda;
    }

    public DiscordNodeTypes getNodeTypes() {
        return this.nodeTypes;
    }

    public Stream<Guild> getUserGuilds(User user) {
        return this.jda.getGuilds().stream()
            .filter(g -> this.userHasAdminPermission(user, g));
    }

    public boolean userHasAdminPermission(User user, Guild guild) {
        String id = user.getDiscordId();
        if (id == null) {
            return false;
        }
        try {
            var member = guild.retrieveMember(UserSnowflake.fromId(id)).complete();
            return member != null && member.hasPermission(Permission.ADMINISTRATOR);
        } catch (Exception e) {
            return false;
        }
    }

    public List<GraphValidityError> validateUserHasPermission(Node node, String guildIdKey, Either<Flow, Graph> flow) {
        if (flow.isRight()) {
            return List.of();
        }
        User user = User.objects.getUserByUserId(flow.left().getAuthorId().toString());
        if (user == null) {
            return List.of();
        }

        var guildId = DataBox.get(node.settings(), guildIdKey, DiscordDataTypes.GUILD_ID)
            .filter(Predicate.not(String::isBlank))
            .map(String::trim);
        if (guildId.isPresent()) {
            return guildId
                .map(this.jda::getGuildById)
                .filter(guild -> !this.userHasAdminPermission(user, guild))
                .map(guild -> new GraphValidityError(node.id(), "You do not have Administrator permission in this Discord server."))
                .stream().toList();
        } else {
            return List.of(new GraphValidityError(node.id(), "You must specify a Discord server."));
        }
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
