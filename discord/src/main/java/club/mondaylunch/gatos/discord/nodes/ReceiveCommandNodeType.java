package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

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

public class ReceiveCommandNodeType extends NodeType.Start<SlashCommandInteractionEvent> {
    private final GatosDiscord gatosDiscord;

    public ReceiveCommandNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Graph graph) {
        var canFindReply = graph.nodes().stream().anyMatch(n -> n.type().equals(this.gatosDiscord.getNodeTypes().commandReply()));
        return GatosUtils.union(super.isValid(node, graph), canFindReply ? Set.of() : Set.of(new GraphValidityError(node.id(), "No command reply node found.")));
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "guild_id", DiscordDataTypes.GUILD_ID.create(""),
            "command_name", DataType.STRING.create("")
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
        Guild guild = this.gatosDiscord.getJda().getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Guild not found: " + guildId);
        }
        this.gatosDiscord.createSlashCommandListener(node.id(), commandName, guild, s -> {
            function.accept(s);
        });
    }

    @Override
    public void teardownFlow(Flow flow, Node node) {
        this.gatosDiscord.removeSlashCommandListener(node.id());
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(@Nullable SlashCommandInteractionEvent event, Map<String, DataBox<?>> settings) {
        if (event == null) {
            return Map.of(
                "user", CompletableFuture.completedFuture(DiscordDataTypes.USER_ID.create("")),
                "channel", CompletableFuture.completedFuture(DiscordDataTypes.CHANNEL_ID.create(""))
            );
        }
        return Map.of(
            "user", CompletableFuture.completedFuture(DiscordDataTypes.USER_ID.create(event.getUser().getId())),
            "channel", CompletableFuture.completedFuture(DiscordDataTypes.CHANNEL_ID.create(event.getChannel().getId())),
            "command_event", CompletableFuture.completedFuture(DiscordDataTypes.SLASH_COMMAND_EVENT.create(event))
        );
    }
}
