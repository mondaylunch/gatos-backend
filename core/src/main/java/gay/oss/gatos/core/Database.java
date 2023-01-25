package gay.oss.gatos.core;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;

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
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import gay.oss.gatos.core.models.Flow;
import gay.oss.gatos.core.models.User;

/**
 * Singleton instance of the MongoDB client.
 */
public enum Database {

    INSTANCE;

    private final MongoClient client = createClient();
    private final CodecRegistry codecRegistry = createRegistry();

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
        ClassModel<User> userModel = ClassModel.builder(User.class)
            .conventions(List.of(Conventions.ANNOTATION_CONVENTION)).build();

        ClassModel<Flow> flowModel = ClassModel.builder(Flow.class)
            .conventions(List.of(Conventions.ANNOTATION_CONVENTION)).build();

        // Register classes and create the registry
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(userModel, flowModel).build();
        return fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
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
        return INSTANCE.client.getDatabase("gatos").withCodecRegistry(INSTANCE.codecRegistry);
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
}
