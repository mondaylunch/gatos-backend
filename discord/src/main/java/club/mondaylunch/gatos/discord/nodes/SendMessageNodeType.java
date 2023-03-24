package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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

public class SendMessageNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public SendMessageNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "guild_id", DiscordDataTypes.GUILD_ID.create("")
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
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "channel_id", DiscordDataTypes.CHANNEL_ID),
            new NodeConnector.Input<>(nodeId, "message_text", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "message_embed", DiscordDataTypes.MESSAGE_EMBED.optionalOf())
        );
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String guildId = DataBox.get(settings, "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        String channelId = DataBox.get(inputs, "channel_id", DiscordDataTypes.CHANNEL_ID).orElseThrow();
        String messageText = DataBox.get(inputs, "message_text", DataType.STRING).orElseThrow();
        Optional<EmbedBuilder> messageEmbeds = DataBox.get(inputs, "message_embed", DiscordDataTypes.MESSAGE_EMBED.optionalOf()).flatMap(Function.identity());
        Guild guild = this.gatosDiscord.getJda().getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Guild not found: " + guildId);
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalStateException("Channel not found: " + channelId);
        }
        return channel.sendMessage(new MessageCreateBuilder()
            .addContent(messageText)
            .addEmbeds(messageEmbeds.map(EmbedBuilder::build).map(List::of).orElse(List.of()).toArray(MessageEmbed[]::new))
            .build()).submit().thenAccept($ -> {});
    }
}
