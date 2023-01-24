package gay.oss.gatos.core;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Single source of truth about the environment
 */
public class Environment {
    private static Environment INSTANCE = new Environment();
    private Dotenv env;

    /**
     * Load configuration from disk and process environment
     */
    private Environment() {
        this.env = Dotenv
                .configure()
                .directory("..")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }

    /**
     * Get the MongoDB connection URI
     * 
     * @return String
     */
    public static String getMongoUri() {
        return INSTANCE.env.get("MONGODB_URI", "mongodb://localhost");
    }

    /**
     * Get the Redis connection URI
     * 
     * @return String
     */
    public static String getRedisUri() {
        return INSTANCE.env.get("REDIS_URI", "redis://localhost");
    }
}
