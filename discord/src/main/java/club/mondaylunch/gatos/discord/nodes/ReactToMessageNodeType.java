package club.mondaylunch.gatos.discord.nodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.DiscordDataTypes;
import club.mondaylunch.gatos.discord.GatosDiscord;

public class ReactToMessageNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public ReactToMessageNodeType(GatosDiscord gatosDiscord) {
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
            new NodeConnector.Input<>(nodeId, "emoji_id", DiscordDataTypes.EMOJI_ID)
        );
    }

    @Override
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        Message message = DataBox.get(settings, "message", DiscordDataTypes.MESSAGE).orElseThrow();
        String emojiId = DataBox.get(inputs, "emoji_id", DiscordDataTypes.EMOJI_ID).orElseThrow();
        Guild guild = message.getGuild();
        Emoji emoji = guild.getEmojiById(emojiId);
        if (emoji == null) {
            throw new IllegalStateException("Emoji not found: " + emojiId);
        }
        return message.addReaction(emoji).submit().thenAccept($ -> {});
    }
}
