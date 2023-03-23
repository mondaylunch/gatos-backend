package club.mondaylunch.gatos.discord.nodes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.DiscordDataTypes;
import club.mondaylunch.gatos.discord.GatosDiscord;

public class ReplyToMessageNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public ReplyToMessageNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "message", DiscordDataTypes.MESSAGE),
            new NodeConnector.Input<>(nodeId, "reply_text", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "reply_embed", DiscordDataTypes.MESSAGE_EMBED.optionalOf())
        );
    }

    @Override
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        Message message = DataBox.get(inputs, "message", DiscordDataTypes.MESSAGE).orElseThrow();
        String replyText = DataBox.get(inputs, "reply_text", DataType.STRING).orElseThrow();
        Optional<EmbedBuilder> replyEmbed = DataBox.get(inputs, "reply_embed", DiscordDataTypes.MESSAGE_EMBED.optionalOf()).flatMap(Function.identity());
        return message.reply(new MessageCreateBuilder()
            .addContent(replyText)
            .addEmbeds(replyEmbed.map(EmbedBuilder::build).map(List::of).orElse(List.of()).toArray(MessageEmbed[]::new))
            .build()).submit().thenAccept($ -> {
        });
    }
}
