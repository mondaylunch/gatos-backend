package club.mondaylunch.gatos.discord;

import java.util.function.Function;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;

import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.SettingWidgets;

public class DiscordDataTypes {
    public static final DataType<String> GUILD_ID = DataType.register("discord.guild_id", String.class);
    public static final DataType<String> CHANNEL_ID = DataType.register("discord.channel_id", String.class);
    public static final DataType<String> USER_ID = DataType.register("discord.user_id", String.class);
    public static final DataType<String> ROLE_ID = DataType.register("discord.role_id", String.class);
    public static final DataType<String> EMOJI_ID = DataType.register("discord.emoji_id", String.class);
    public static final DataType<SlashCommandEvent> SLASH_COMMAND_EVENT = DataType.register("discord.slash_command_event", SlashCommandEvent.class);
    public static final DataType<Message> MESSAGE = DataType.register("discord.message", Message.class);
    public static final DataType<EmbedBuilder> MESSAGE_EMBED = DataType.register("discord.message_embed", EmbedBuilder.class);

    static void init(GatosDiscord discord) {
        Conversions.register(DataType.STRING, GUILD_ID, Function.identity());
        Conversions.register(DataType.STRING, CHANNEL_ID, Function.identity());
        Conversions.register(DataType.STRING, USER_ID, Function.identity());
        Conversions.register(DataType.STRING, ROLE_ID, Function.identity());
        Conversions.register(DataType.STRING, EMOJI_ID, Function.identity());
        SettingWidgets.register(GUILD_ID, SettingWidgets.Widget.dropdown(u -> discord.getUserGuilds(u).map(ISnowflake::getId).toList()));
        SettingWidgets.register(CHANNEL_ID, SettingWidgets.Widget.dropdown(u -> discord.getUserGuilds(u).flatMap(g -> g.getTextChannelCache().stream()).map(ISnowflake::getId).toList()));
        SettingWidgets.register(ROLE_ID, SettingWidgets.Widget.dropdown(u -> discord.getUserGuilds(u).flatMap(g -> g.getRoleCache().stream()).map(ISnowflake::getId).toList()));
        SettingWidgets.register(EMOJI_ID, SettingWidgets.Widget.dropdown(u -> discord.getUserGuilds(u).flatMap(g -> g.getEmojiCache().stream()).map(ISnowflake::getId).toList()));
    }
}
