package club.mondaylunch.gatos.discord;

import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.nodes.ApplyRoleNodeType;
import club.mondaylunch.gatos.discord.nodes.CommandReplyNodeType;
import club.mondaylunch.gatos.discord.nodes.CreateEmbedNodeType;
import club.mondaylunch.gatos.discord.nodes.ReactToMessageNodeType;
import club.mondaylunch.gatos.discord.nodes.ReceiveCommandNodeType;
import club.mondaylunch.gatos.discord.nodes.ReceiveMessageNodeType;
import club.mondaylunch.gatos.discord.nodes.ReplyToMessageNodeType;
import club.mondaylunch.gatos.discord.nodes.SendMessageNodeType;
import club.mondaylunch.gatos.discord.nodes.UsersWithRoleNodeType;

public class DiscordNodeTypes {
    private final ReceiveCommandNodeType receiveCommandNodeType;
    private final CommandReplyNodeType commandReplyNodeType;
    private final SendMessageNodeType sendMessageNodeTypeType;
    private final ApplyRoleNodeType applyRoleNodeType;
    private final UsersWithRoleNodeType usersWithRoleNodeType;
    private final ReceiveMessageNodeType receiveMessageNodeType;
    private final ReactToMessageNodeType reactToMessageNodeType;
    private final ReplyToMessageNodeType replyToMessageNodeType;
    private final CreateEmbedNodeType createEmbedNodeType;

    public DiscordNodeTypes(GatosDiscord gatosDiscord) {
        this.receiveCommandNodeType = NodeType.REGISTRY.register("discord.receive_command", new ReceiveCommandNodeType(gatosDiscord));
        this.commandReplyNodeType = NodeType.REGISTRY.register("discord.reply_to_command", new CommandReplyNodeType(gatosDiscord));
        this.sendMessageNodeTypeType = NodeType.REGISTRY.register("discord.send_message", new SendMessageNodeType(gatosDiscord));
        this.applyRoleNodeType = NodeType.REGISTRY.register("discord.apply_role", new ApplyRoleNodeType(gatosDiscord));
        this.usersWithRoleNodeType = NodeType.REGISTRY.register("discord.users_with_role", new UsersWithRoleNodeType(gatosDiscord));
        this.receiveMessageNodeType = NodeType.REGISTRY.register("discord.receive_message", new ReceiveMessageNodeType(gatosDiscord));
        this.reactToMessageNodeType = NodeType.REGISTRY.register("discord.react_to_message", new ReactToMessageNodeType(gatosDiscord));
        this.replyToMessageNodeType = NodeType.REGISTRY.register("discord.reply_to_message", new ReplyToMessageNodeType(gatosDiscord));
        this.createEmbedNodeType = NodeType.REGISTRY.register("discord.create_embed", new CreateEmbedNodeType(gatosDiscord));
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

    public UsersWithRoleNodeType usersWithRole() {
        return this.usersWithRoleNodeType;
    }

    public ReceiveMessageNodeType receiveMessage() {
        return this.receiveMessageNodeType;
    }

    public ReactToMessageNodeType reactToMessage() {
        return this.reactToMessageNodeType;
    }

    public ReplyToMessageNodeType replyToMessage() {
        return this.replyToMessageNodeType;
    }

    public CreateEmbedNodeType createEmbed() {
        return this.createEmbedNodeType;
    }
}