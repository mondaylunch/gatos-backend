package club.mondaylunch.gatos.discord;

import club.mondaylunch.gatos.core.graph.type.NodeType;

public class DiscordNodeTypes {
    private final DiscordReceiveCommandNodeType receiveCommandNodeType;
    private final DiscordCommandReplyNodeType commandReplyNodeType;
    private final SendDiscordMessageNode sendDiscordMessageNodeType;
    private final ApplyRoleNodeType applyRoleNodeType;

    public DiscordNodeTypes(GatosDiscord gatosDiscord) {
        this.receiveCommandNodeType = NodeType.REGISTRY.register("discord.receive_command", new DiscordReceiveCommandNodeType(gatosDiscord));
        this.commandReplyNodeType = NodeType.REGISTRY.register("discord.reply_to_command", new DiscordCommandReplyNodeType(gatosDiscord));
        this.sendDiscordMessageNodeType = NodeType.REGISTRY.register("discord.send_message", new SendDiscordMessageNode(gatosDiscord::getJda));
        this.applyRoleNodeType = NodeType.REGISTRY.register("discord.apply_role", new ApplyRoleNodeType(gatosDiscord::getJda));
    }

    public SendDiscordMessageNode sendDiscordMessage() {
        return this.sendDiscordMessageNodeType;
    }

    public DiscordCommandReplyNodeType commandReply() {
        return this.commandReplyNodeType;
    }

    public DiscordReceiveCommandNodeType receiveCommand() {
        return this.receiveCommandNodeType;
    }

    public ApplyRoleNodeType applyRole() {
        return this.applyRoleNodeType;
    }
}
