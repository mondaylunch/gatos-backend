package club.mondaylunch.gatos.discord;

import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.nodes.ApplyRoleNodeType;
import club.mondaylunch.gatos.discord.nodes.CommandReplyNodeType;
import club.mondaylunch.gatos.discord.nodes.ReceiveCommandNodeType;
import club.mondaylunch.gatos.discord.nodes.SendMessageNodeType;

public class DiscordNodeTypes {
    private final ReceiveCommandNodeType receiveCommandNodeType;
    private final CommandReplyNodeType commandReplyNodeType;
    private final SendMessageNodeType sendMessageNodeTypeType;
    private final ApplyRoleNodeType applyRoleNodeType;

    public DiscordNodeTypes(GatosDiscord gatosDiscord) {
        this.receiveCommandNodeType = NodeType.REGISTRY.register("discord.receive_command", new ReceiveCommandNodeType(gatosDiscord));
        this.commandReplyNodeType = NodeType.REGISTRY.register("discord.reply_to_command", new CommandReplyNodeType(gatosDiscord));
        this.sendMessageNodeTypeType = NodeType.REGISTRY.register("discord.send_message", new SendMessageNodeType(gatosDiscord::getJda));
        this.applyRoleNodeType = NodeType.REGISTRY.register("discord.apply_role", new ApplyRoleNodeType(gatosDiscord::getJda));
    }

    public SendMessageNodeType sendDiscordMessage() {
        return this.sendMessageNodeTypeType;
    }

    public CommandReplyNodeType commandReply() {
        return this.commandReplyNodeType;
    }

    public ReceiveCommandNodeType receiveCommand() {
        return this.receiveCommandNodeType;
    }

    public ApplyRoleNodeType applyRole() {
        return this.applyRoleNodeType;
    }
}
