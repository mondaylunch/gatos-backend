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
import club.mondaylunch.gatos.core.models.FlowData;

public class FlowDataCollection {

    private final MongoCollection<FlowData> collection;

    public FlowDataCollection() {
        this.collection = Database.getCollection("flow_data", FlowData.class);
    }

    public Optional<DataBox<?>> get(UUID flowId, String key) {
        var id = new FlowData.Id(flowId, key);
        var flowData = this.collection.find(Filters.eq(id))
            .limit(1)
            .first();
        if (flowData == null) {
            return Optional.empty();
        } else {
            return Optional.of(flowData.getValue());
        }
    }

    public void set(UUID flowId, String key, DataBox<?> value) {
        var flowData = new FlowData(flowId, key, value);
        this.collection.replaceOne(Filters.eq(flowData.getId()), flowData, new ReplaceOptions().upsert(true));
    }

    public void setIfAbsent(UUID flowId, String key, DataBox<?> value) {
        var id = new FlowData.Id(flowId, key);
        var flowData = new FlowData(flowId, key, value);
        var document = new BsonDocument();
        var context = EncoderContext.builder().build();
        var codec = Database.getCodecRegistry().get(FlowData.class);
        try (var writer = new BsonDocumentWriter(document)) {
            codec.encode(writer, flowData, context);
        }
        var update = Updates.setOnInsert(document);
        this.collection.updateOne(Filters.eq(id), update, new UpdateOptions().upsert(true));
    }

    public void increment(UUID flowId, String key, Number value) {
        var id = new FlowData.Id(flowId, key);
        var update = Updates.inc("value.value", value);
        this.collection.updateOne(Filters.eq(id), update);
    }

    public void incrementOrSet(UUID flowId, String key, Number value) {
        var id = new FlowData.Id(flowId, key);
        var update = Updates.combine(
            Updates.set("_id", id),
            Updates.set("value.type", DataType.NUMBER.name()),
            Updates.inc("value.value", value)
        );
        this.collection.updateOne(Filters.eq(id), update, new UpdateOptions().upsert(true));
    }

    public void multiply(UUID flowId, String key, Number value) {
        var id = new FlowData.Id(flowId, key);
        var update = Updates.mul("value.value", value);
        this.collection.updateOne(Filters.eq(id), update);
    }

    public void remove(UUID flowId, String key) {
        var id = new FlowData.Id(flowId, key);
        this.collection.deleteOne(Filters.eq(id));
    }

    public boolean contains(UUID flowId, String key) {
        var id = new FlowData.Id(flowId, key);
        return collection.countDocuments(Filters.eq(id), new CountOptions().limit(1)) > 0;
    }

    public boolean contains(UUID flowId, String key, DataType<?> type) {
        var id = new FlowData.Id(flowId, key);
        return collection.countDocuments(
            Filters.and(
                Filters.eq(id),
                Filters.eq("value.type", type.name())
            ),
            new CountOptions().limit(1)
        ) > 0;
    }

    public void clear() {
        this.collection.drop();
    }
}
