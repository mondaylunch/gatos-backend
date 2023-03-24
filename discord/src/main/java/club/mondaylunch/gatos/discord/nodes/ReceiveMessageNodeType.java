package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

public class ReceiveMessageNodeType extends NodeType.Start<MessageReceivedEvent> {
    private final GatosDiscord gatosDiscord;

    public ReceiveMessageNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "guild_id", DiscordDataTypes.GUILD_ID.create(""),
            "channel_ids", DiscordDataTypes.CHANNEL_ID.listOf().create(List.of())
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> flowOrGraph) {
        return GatosUtils.union(
            super.isValid(node, flowOrGraph),
            GraphValidityError.ensureSetting(node, "guild_id", DiscordDataTypes.GUILD_ID, s -> s.isBlank() ? "A Discord server must be set" : null),
            this.gatosDiscord.validateUserHasPermission(node, "guild_id", flowOrGraph)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "user", DiscordDataTypes.USER_ID),
            new NodeConnector.Output<>(nodeId, "channel", DiscordDataTypes.CHANNEL_ID),
            new NodeConnector.Output<>(nodeId, "message", DiscordDataTypes.MESSAGE)
        );
    }

    @Override
    public void setupFlow(Flow flow, Consumer<@Nullable MessageReceivedEvent> function, Node node) {
        String guildId = DataBox.get(node.settings(), "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        List<String> channelIds = DataBox.get(node.settings(), "channel_ids", DiscordDataTypes.CHANNEL_ID.listOf()).orElseThrow();
        if (!guildId.isEmpty()) {
            Guild guild = this.gatosDiscord.getJda().getGuildById(guildId);
            if (guild != null) {
                this.gatosDiscord.createEventListener(node.id(), MessageReceivedEvent.class, m -> {
                    if (m != null
                        && !this.gatosDiscord.getJda().getSelfUser().equals(m.getAuthor())
                        && m.isFromGuild() && m.getGuild().getId().equals(guildId)
                        && (channelIds.isEmpty() || channelIds.contains(m.getChannel().getId()))
                    ) {
                        function.accept(m);
                    }
                });
            }
        }
    }

    @Override
    public void teardownFlow(Flow flow, Node node) {
        this.gatosDiscord.removeEventListener(node.id());
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, @Nullable MessageReceivedEvent event, Map<String, DataBox<?>> settings) {
        if (event == null) {
            throw new IllegalStateException();
        }

        return Map.of(
            "user", CompletableFuture.completedFuture(DiscordDataTypes.USER_ID.create(event.getAuthor().getId())),
            "channel", CompletableFuture.completedFuture(DiscordDataTypes.CHANNEL_ID.create(event.getChannel().getId())),
            "message", CompletableFuture.completedFuture(DiscordDataTypes.MESSAGE.create(event.getMessage()))
        );
    }
}
