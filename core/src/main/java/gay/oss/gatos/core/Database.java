package gay.oss.gatos.core;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Arrays;

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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gay.oss.gatos.core.models.Flow;
import gay.oss.gatos.core.models.User;

public class Database {
    private static Database INSTANCE = new Database();
    private MongoClient client;
    private CodecRegistry codecRegistry;

    private Database() {
        ClassModel<User> userModel = ClassModel.builder(User.class)
                .conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).build();

        ClassModel<Flow> flowModel = ClassModel.builder(Flow.class)
                .conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).build();

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(userModel, flowModel).build();
        this.codecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/test");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(connectionString).build();

        this.client = MongoClients.create(mongoClientSettings);
    }

    public static void checkConnection() {
        MongoDatabase database = INSTANCE.client.getDatabase("admin");

        Bson command = new BsonDocument("ping", new BsonInt64(1));
        database.runCommand(command);
    }

    public static MongoDatabase getDatabase() {
        return INSTANCE.client.getDatabase("gatos").withCodecRegistry(INSTANCE.codecRegistry);
    }

    public static MongoCollection<Document> getCollection(String name) {
        return getDatabase().getCollection(name);
    }

    public static <TDocument> MongoCollection<TDocument> getCollection(String name, Class<TDocument> cls) {
        return getDatabase().getCollection(name, cls);
    }
}
