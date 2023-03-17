package club.mondaylunch.gatos.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.data.DataType;

public class DiscordDataTypes {
    public static final DataType<String> GUILD_ID = DataType.register("discord.guild_id", String.class);
    public static final DataType<String> CHANNEL_ID = DataType.register("discord.channel_id", String.class);
    public static final DataType<String> USER_ID = DataType.register("discord.user_id", String.class);
    public static final DataType<String> ROLE_ID = DataType.register("discord.role_id", String.class);
    public static final DataType<@Nullable SlashCommandInteraction> SLASH_COMMAND_EVENT = DataType.register("discord.slash_command_event", SlashCommandInteraction.class);
    public static final DataType<Message> MESSAGE = DataType.register("discord.message", Message.class);
    public static final DataType<EmbedBuilder> MESSAGE_EMBED = DataType.register("discord.message_embed", EmbedBuilder.class);

    static void init() {

    }
}
