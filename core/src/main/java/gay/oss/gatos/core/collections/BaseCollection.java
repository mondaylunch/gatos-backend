package gay.oss.gatos.core.collections;

import static com.mongodb.client.model.Filters.eq;

import java.util.UUID;

import com.mongodb.client.MongoCollection;

import gay.oss.gatos.core.Database;
import gay.oss.gatos.core.models.BaseModel;

public class BaseCollection<T extends BaseModel> {
    private MongoCollection<T> collection;

    public BaseCollection(String collectionName, Class<T> cls) {
        this.collection = Database.getCollection(collectionName, cls);
    }

    public MongoCollection<T> getCollection() {
        return this.collection;
    }

    public void insert(T obj) {
        if (obj.getId() == null) {
            obj.setId(UUID.randomUUID());
        }

        this.getCollection().insertOne(obj);
    }

    public T getById(UUID id) {
        return this.getCollection().find(eq("_id", id)).first();
    }
}
