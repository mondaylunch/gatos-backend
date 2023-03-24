package club.mondaylunch.gatos.core.collection;

import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.EncoderContext;

import club.mondaylunch.gatos.core.Database;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.UserData;

public class UserDataCollection {

    private final MongoCollection<UserData> collection;

    public UserDataCollection() {
        this.collection = Database.getCollection("user_data", UserData.class);
    }

    public Optional<DataBox<?>> get(UUID userId, String key) {
        var id = new UserData.Id(userId, key);
        var userData = this.collection.find(Filters.eq(id))
            .limit(1)
            .first();
        if (userData == null) {
            return Optional.empty();
        } else {
            return Optional.of(userData.getValue());
        }
    }

    public void set(UUID userId, String key, DataBox<?> value) {
        var userData = new UserData(userId, key, value);
        this.collection.replaceOne(Filters.eq(userData.getId()), userData, new ReplaceOptions().upsert(true));
    }

    public void setIfAbsent(UUID userId, String key, DataBox<?> value) {
        var id = new UserData.Id(userId, key);
        var userData = new UserData(userId, key, value);
        var document = new BsonDocument();
        var context = EncoderContext.builder().build();
        var codec = Database.getCodecRegistry().get(UserData.class);
        try (var writer = new BsonDocumentWriter(document)) {
            codec.encode(writer, userData, context);
        }
        var update = Updates.setOnInsert(document);
        this.collection.updateOne(Filters.eq(id), update, new UpdateOptions().upsert(true));
    }

    public void increment(UUID userId, String key, Number value) {
        var id = new UserData.Id(userId, key);
        var update = Updates.inc("value.value", value);
        this.collection.updateOne(Filters.eq(id), update);
    }

    public void incrementOrSet(UUID userId, String key, Number value) {
        var id = new UserData.Id(userId, key);
        var update = Updates.combine(
            Updates.set("_id", id),
            Updates.set("value.type", DataType.NUMBER.name()),
            Updates.inc("value.value", value)
        );
        this.collection.updateOne(Filters.eq(id), update, new UpdateOptions().upsert(true));
    }

    public void multiply(UUID userId, String key, Number value) {
        var id = new UserData.Id(userId, key);
        var update = Updates.mul("value.value", value);
        this.collection.updateOne(Filters.eq(id), update);
    }

    public void delete(UUID userId, String key) {
        var id = new UserData.Id(userId, key);
        this.collection.deleteOne(Filters.eq(id));
    }

    public void delete(UUID userId) {
        this.collection.deleteMany(Filters.eq("_id.user_id", userId));
    }

    public boolean contains(UUID userId, String key) {
        var id = new UserData.Id(userId, key);
        return this.collection.countDocuments(Filters.eq(id), new CountOptions().limit(1)) > 0;
    }

    public boolean contains(UUID userId, String key, DataType<?> type) {
        var id = new UserData.Id(userId, key);
        return this.collection.countDocuments(
            Filters.and(
                Filters.eq(id),
                Filters.eq("value.type", type.name())
            ),
            new CountOptions().limit(1)
        ) > 0;
    }

    public long size() {
        return this.collection.countDocuments();
    }

    public void clear() {
        this.collection.drop();
    }
}
