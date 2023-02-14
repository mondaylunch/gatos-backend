package club.mondaylunch.gatos.core;

import club.mondaylunch.gatos.core.codec.GraphCodecProvider;
import club.mondaylunch.gatos.core.codec.NodeCodecProvider;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import club.mondaylunch.gatos.core.codec.ClassModelRegistry;
import club.mondaylunch.gatos.core.codec.DataTypeCodec;
import club.mondaylunch.gatos.core.codec.NodeConnectionCodecProvider;
import club.mondaylunch.gatos.core.codec.NodeConnectorCodecProvider;
import club.mondaylunch.gatos.core.codec.NodeTypeCodec;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

/**
 * Singleton instance of the MongoDB client.
 */
public enum Database {

    INSTANCE;

    private final MongoClient client = createClient();
    private CodecRegistry codecRegistry = createRegistry();

    /**
     * Configure the MongoDB driver.
     */
    private static MongoClient createClient() {
        // Configure the connection settings
        ConnectionString connectionString = new ConnectionString(Environment.getMongoUri());
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(connectionString).build();

        // Build the client
        return MongoClients.create(mongoClientSettings);
    }

    /**
     * Configure the MongoDB driver.
     */
    private static CodecRegistry createRegistry() {
        // Create ClassModel for every single model
        // Documentation:
        // https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/data-formats/pojo-customization/#customize-a-pojocodecprovider

        // Register classes
        registerClassModels();

        // Create the registry
        return CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(
                NodeTypeCodec.INSTANCE,
                DataTypeCodec.INSTANCE
            ),
            CodecRegistries.fromProviders(
                NodeConnectorCodecProvider.INSTANCE,
                NodeConnectionCodecProvider.INSTANCE,
                GraphCodecProvider.INSTANCE,
                NodeCodecProvider.INSTANCE
            ),
            ClassModelRegistry.createCodecRegistry(),
            MongoClientSettings.getDefaultCodecRegistry()
        );
    }

    /**
     * Refresh the codec registry with any new changes.
     */
    public static void refreshCodecRegistry() {
        INSTANCE.codecRegistry = createRegistry();
    }

    /**
     * Check whether we can talk with the database.
     */
    public static void checkConnection() {
        MongoDatabase database = INSTANCE.client.getDatabase("admin");
        Bson command = new BsonDocument("ping", new BsonInt64(1));
        database.runCommand(command);
    }

    /**
     * Get the Mongo Database.
     *
     * @return {@link MongoDatabase}
     */
    public static MongoDatabase getDatabase() {
        return INSTANCE.client.getDatabase(Environment.isJUnitTest() ? "gatos-testdb" : "gatos")
            .withCodecRegistry(INSTANCE.codecRegistry);
    }

    /**
     * Get a Mongo Collection by name.
     *
     * @return {@link MongoCollection}
     */
    public static MongoCollection<Document> getCollection(String name) {
        return getDatabase().getCollection(name);
    }

    /**
     * Get a Mongo Collection with POJO support by name and class.
     *
     * @return {@link MongoCollection}
     */
    public static <TDocument> MongoCollection<TDocument> getCollection(String name, Class<TDocument> cls) {
        return getDatabase().getCollection(name, cls);
    }

    private static void registerClassModels() {
        ClassModelRegistry.register(
            User.class,
            Flow.class,
            Graph.class,
            Node.class,
            DataBox.class
        );
    }
}
