package club.mondaylunch.gatos.discord;

import java.io.IOException;
import java.util.Properties;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import club.mondaylunch.gatos.core.GatosPlugin;

public class GatosDiscord implements GatosPlugin {
    private JDA jda;

    @Override
    public void init() {
        if (this.jda != null) {
            this.jda.shutdown();
        }

        this.jda = JDABuilder.createDefault(this.getToken())
            .setActivity(Activity.competing("SEG"))
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
            .build();
    }

    private String getToken() {
        var properties = new Properties();
        try (var stream = this.getClass().getResourceAsStream("/gatos_discord.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failure loading discord token", e);
        }
        return properties.getProperty("DISCORD_TOKEN");
    }
}
