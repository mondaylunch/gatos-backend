package club.mondaylunch.gatos.core;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Single source of truth about the environment.
 */
public enum Environment {

    INSTANCE;

    /**
     * Load configuration from disk and process environment.
     */
    private final Dotenv env = Dotenv.configure()
            .directory("..")
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    /**
     * Get the MongoDB connection URI.
     *
     * @return String
     */
    public static String getMongoUri() {
        return INSTANCE.env.get("MONGODB_URI", "mongodb://localhost");
    }

    /**
     * Get the Redis connection URI.
     *
     * @return String
     */
    @SuppressWarnings("unused")
    public static String getRedisUri() {
        return INSTANCE.env.get("REDIS_URI", "redis://localhost");
    }

    /**
     * Get the Discord bot token.
     * @return the discord token
     */
    public static String getDiscordToken() {
        return INSTANCE.env.get("DISCORD_TOKEN", "");
    }

    /**
     * Check whether we are in a JUnit test.
     * <a href="https://stackoverflow.com/a/12717377">Source</a>
     *
     * @return whether we are in a JUnit test
     */
    public static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
