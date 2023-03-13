package club.mondaylunch.gatos.discord;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class DiscordCommandReplyNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public DiscordCommandReplyNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "is_ephemeral", DataType.BOOLEAN.create(false)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "content", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "event", DiscordDataTypes.SLASH_COMMAND_EVENT)
        );
    }

    @Override
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String content = DataBox.get(inputs, "content", DataType.STRING).orElse("");
        boolean isEphemeral = DataBox.get(settings, "is_ephemeral", DataType.BOOLEAN).orElse(false);
        var event = DataBox.get(inputs, "event", DiscordDataTypes.SLASH_COMMAND_EVENT).orElseThrow();
        return event.reply(content).setEphemeral(isEphemeral).submit().thenAccept($ -> {});
    }
}
