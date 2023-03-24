package club.mondaylunch.gatos.discord;

public record SlashCommandEvent(
    java.util.concurrent.CompletableFuture<net.dv8tion.jda.api.interactions.InteractionHook> action) {

}
