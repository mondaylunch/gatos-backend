package club.mondaylunch.gatos.discord;

import club.mondaylunch.gatos.core.data.DataType;

public class DiscordDataTypes {
    public static final DataType<String> GUILD_ID = DataType.register("discord.guild_id", String.class);
    public static final DataType<String> CHANNEL_ID = DataType.register("discord.channel_id", String.class);
    public static final DataType<String> USER_ID = DataType.register("discord.user_id", String.class);
}
