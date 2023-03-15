package club.mondaylunch.gatos.discord;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ApplyRoleNodeType extends NodeType.End {
    private final Supplier<JDA> jda;

    public ApplyRoleNodeType(Supplier<JDA> jda) {
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
            new NodeConnector.Input<>(nodeId, "role_id", DiscordDataTypes.ROLE_ID),
            new NodeConnector.Input<>(nodeId, "user_id", DiscordDataTypes.USER_ID)
        );
    }

    @Override
    public CompletableFuture<Void> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        String guildId = DataBox.get(settings, "guild_id", DiscordDataTypes.GUILD_ID).orElseThrow();
        String userId = DataBox.get(inputs, "user_id", DiscordDataTypes.USER_ID).orElseThrow();
        String roleId = DataBox.get(inputs, "role_id", DiscordDataTypes.ROLE_ID).orElseThrow();
        Guild guild = this.jda.get().getGuildById(guildId);
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
