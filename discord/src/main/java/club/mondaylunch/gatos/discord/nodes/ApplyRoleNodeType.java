package club.mondaylunch.gatos.discord.nodes;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

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

public class ApplyRoleNodeType extends NodeType.End {
    private final GatosDiscord gatosDiscord;

    public ApplyRoleNodeType(GatosDiscord gatosDiscord) {
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
        return GatosUtils.union(super.isValid(node, flowOrGraph), this.gatosDiscord.validateUserHasPermission(node, "guild_id", flowOrGraph));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "role_id", DiscordDataTypes.ROLE_ID),
            new NodeConnector.Input<>(nodeId, "user_id", DiscordDataTypes.USER_ID)
        );
    }

    @Override
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String guildId = DataBox.get(settings, "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        String userId = DataBox.get(inputs, "user_id", DiscordDataTypes.USER_ID).orElseThrow();
        String roleId = DataBox.get(inputs, "role_id", DiscordDataTypes.ROLE_ID).orElseThrow();
        Guild guild = this.gatosDiscord.getJda().getGuildById(guildId);
        if (guild == null) {
            throw new IllegalStateException("Guild not found: " + guildId);
        }
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            throw new IllegalStateException("Role not found: " + roleId);
        }
        return guild.addRoleToMember(User.fromId(userId), role).submit().thenAccept($ -> {});
    }
}
