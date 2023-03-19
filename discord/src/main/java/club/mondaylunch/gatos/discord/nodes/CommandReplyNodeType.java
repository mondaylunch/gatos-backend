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
        return Map.of(
            "is_ephemeral", DataType.BOOLEAN.create(false)
        );
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
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String content = DataBox.get(inputs, "content", DataType.STRING).orElse("");
        boolean isEphemeral = DataBox.get(settings, "is_ephemeral", DataType.BOOLEAN).orElse(false);
        var event = DataBox.get(inputs, "event", DiscordDataTypes.SLASH_COMMAND_EVENT).orElseThrow();
        return event.reply(content).setEphemeral(isEphemeral).submit().thenAccept($ -> {});
    }
}
