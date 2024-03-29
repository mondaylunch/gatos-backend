package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.GatosUtils;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.discord.DiscordDataTypes;
import club.mondaylunch.gatos.discord.GatosDiscord;
import club.mondaylunch.gatos.discord.SlashCommandEvent;

public class ReceiveCommandNodeType extends NodeType.Start<SlashCommandInteractionEvent> {
    private final GatosDiscord gatosDiscord;

    public ReceiveCommandNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "guild_id", DiscordDataTypes.GUILD_ID.create(""),
            "command_name", DataType.STRING.create(""),
            "is_reply_ephemeral", DataType.BOOLEAN.create(false)
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> flowOrGraph) {
        var graph = flowOrGraph.map(Flow::getGraph, Function.identity());
        var canFindReply = graph.nodes().stream().anyMatch(n -> n.type().equals(this.gatosDiscord.getNodeTypes().commandReply()));
        return GatosUtils.union(
            super.isValid(node, flowOrGraph),
            canFindReply ? Set.of() : Set.of(new GraphValidityError(node.id(), "No command reply node found.")),
            GraphValidityError.ensureSetting(node, "guild_id", DiscordDataTypes.GUILD_ID, s -> s.isBlank() ? "A Discord server must be set" : null),
            GraphValidityError.ensureSetting(node, "command_name", DiscordDataTypes.GUILD_ID, s -> s.isBlank() ? "Command name cannot be blank" : null),
            this.gatosDiscord.validateUserHasPermission(node, "guild_id", flowOrGraph)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "user", DiscordDataTypes.USER_ID),
            new NodeConnector.Output<>(nodeId, "channel", DiscordDataTypes.CHANNEL_ID),
            new NodeConnector.Output<>(nodeId, "command_event", DiscordDataTypes.SLASH_COMMAND_EVENT)
        );
    }

    @Override
    public void setupFlow(Flow flow, Consumer<@Nullable SlashCommandInteractionEvent> function, Node node) {
        String guildId = DataBox.get(node.settings(), "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        String commandName = DataBox.get(node.settings(), "command_name", DataType.STRING).orElseThrow();
        if (!guildId.isBlank() && !commandName.isBlank()) {
            Guild guild = this.gatosDiscord.getJda().getGuildById(guildId);
            if (guild != null) {
                this.gatosDiscord.createSlashCommandListener(node.id(), commandName, guild, function);
            }
        }
    }

    @Override
    public void teardownFlow(Flow flow, Node node) {
        this.gatosDiscord.removeSlashCommandListener(node.id());
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, @Nullable SlashCommandInteractionEvent event, Map<String, DataBox<?>> settings) {
        if (event == null) {
            GatosDiscord.LOGGER.info("Received null event for node {}", userId);
            return Map.of(
                "user", CompletableFuture.completedFuture(DiscordDataTypes.USER_ID.create("")),
                "channel", CompletableFuture.completedFuture(DiscordDataTypes.CHANNEL_ID.create("")),
                "command_event", CompletableFuture.completedFuture(DiscordDataTypes.SLASH_COMMAND_EVENT.create(new SlashCommandEvent(null)))
            );
        }

        GatosDiscord.LOGGER.info("Deferring reply for command");
        boolean isEphemeral = DataBox.get(settings, "is_reply_ephemeral", DataType.BOOLEAN).orElse(false);
        return Map.of(
            "user", CompletableFuture.completedFuture(DiscordDataTypes.USER_ID.create(event.getUser().getId())),
            "channel", CompletableFuture.completedFuture(DiscordDataTypes.CHANNEL_ID.create(event.getChannel().getId())),
            "command_event", CompletableFuture.completedFuture(DiscordDataTypes.SLASH_COMMAND_EVENT.create(new SlashCommandEvent(event.deferReply(isEphemeral).submit())))
        );
    }
}
