package gay.oss.gatos.core.collections;

import static com.mongodb.client.model.Filters.eq;

import java.util.UUID;

import com.mongodb.client.MongoCollection;

import gay.oss.gatos.core.Database;
import gay.oss.gatos.core.models.BaseModel;

/**
 * Common Database collection queries
 */
public class BaseCollection<T extends BaseModel> {
    private MongoCollection<T> collection;

    /**
     * Construct a new BaseCollection
     * 
     * @param collectionName Collection name
     * @param cls            Model class
     */
    public BaseCollection(String collectionName, Class<T> cls) {
        this.collection = Database.getCollection(collectionName, cls);
    }

    /**
     * Get the underlying Mongo Collection
     * 
     * @return underlying Mongo Collection
     */
    public MongoCollection<T> getCollection() {
        return this.collection;
    }

    /**
     * Insert a new document into the collection
     * 
     * @param obj POJO
     */
    public void insert(T obj) {
        if (obj.getId() == null) {
            obj.setId(UUID.randomUUID());
        }

        this.getCollection().insertOne(obj);
    }

    /**
     * Get a document by its UUID
     * 
     * @param id UUID
     * @return POJO
     */
    public T getById(UUID id) {
        return this.getCollection().find(eq("_id", id)).first();
    }
}
