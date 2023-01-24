package gay.oss.gatos.core;

import io.github.cdimascio.dotenv.Dotenv;

public class Environment {
    private static Environment INSTANCE = new Environment();
    private Dotenv env;

    private Environment() {
        this.env = Dotenv
            .configure()
            .directory("..")
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();
    }

    public static String getMongoUri() {
        return INSTANCE.env.get("MONGODB_URI", "mongodb://localhost");
    }

    public static String getRedisUri() {
        return INSTANCE.env.get("REDIS_URI", "redis://localhost");
    }
}
