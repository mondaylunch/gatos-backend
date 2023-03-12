package club.mondaylunch.gatos.discord;

import java.io.IOException;
import java.util.Properties;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class GatosDiscord implements GatosPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatosDiscord.class);
    private JDA jda;

    @Override
    public void init() {
        if (this.jda != null) {
            LOGGER.info("Shutting down previous JDA instance");
            this.jda.shutdown();
        }

        try {
            LOGGER.info("Constructing new JDA instance");
            this.jda = JDABuilder.createDefault(this.getToken())
                .setActivity(Activity.competing("SEG"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .build()
                .awaitReady();
            LOGGER.info("JDA ready");
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to set up JDA", e);
        }

        NodeType.REGISTRY.register("discord.send_message", new SendDiscordMessageNode(() -> this.jda));
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
