package club.mondaylunch.gatos.discord.nodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.DiscordDataTypes;

public class SendMessageNodeType extends NodeType.End {
    private final Supplier<JDA> jda;

    public SendMessageNodeType(Supplier<JDA> jda) {
        this.jda = jda;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "guild_id", DiscordDataTypes.GUILD_ID.create("")
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "channel_id", DiscordDataTypes.CHANNEL_ID),
            new NodeConnector.Input<>(nodeId, "message", DataType.STRING)
        );
    }

    @Override
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String guildId = DataBox.get(settings, "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        String channelId = DataBox.get(inputs, "channel_id", DiscordDataTypes.CHANNEL_ID).orElseThrow();
        String message = DataBox.get(inputs, "message", DataType.STRING).orElseThrow();
        Guild guild = this.jda.get().getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Guild not found: " + guildId);
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalStateException("Channel not found: " + channelId);
        }
        return channel.sendMessage(MessageCreateData.fromContent(message)).submit().thenAccept($ -> {});
    }
}
