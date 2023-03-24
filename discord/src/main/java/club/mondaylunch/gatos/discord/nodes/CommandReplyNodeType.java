package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

public class CommandReplyNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public CommandReplyNodeType(GatosDiscord gatosDiscord) {
        this.gatosDiscord = gatosDiscord;
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> flowOrGraph) {
        var graph = flowOrGraph.map(Flow::getGraph, Function.identity());
        var canFindReceive = graph.nodes().stream().anyMatch(n -> n.type().equals(this.gatosDiscord.getNodeTypes().receiveCommand()));
        return GatosUtils.union(
            super.isValid(node, flowOrGraph),
            canFindReceive ? Set.of() : Set.of(new GraphValidityError(node.id(), "No receive command node found."))
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
    public CompletableFuture<Void> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String content = DataBox.get(inputs, "content", DataType.STRING).orElse("");
        content = content.substring(0, Math.min(content.length(), 2000));
        final var finalContent = content;
        var event = DataBox.get(inputs, "event", DiscordDataTypes.SLASH_COMMAND_EVENT).orElseThrow();
        GatosDiscord.LOGGER.info("Going to reply to a command with content: {}", content);
        if (event.action() == null) {
            GatosDiscord.LOGGER.warn("sike!! not gonna do that because the event has no action");
        }
        return event.action() == null
            ? CompletableFuture.runAsync(() -> {})
            : event.action().thenApply(msg -> msg.editOriginal(finalContent).submit()).thenAccept($ -> {});
    }
}
