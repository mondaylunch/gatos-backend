package gay.oss.gatos.core.collections;

import com.mongodb.client.MongoCollection;
import gay.oss.gatos.core.Database;
import gay.oss.gatos.core.models.BaseModel;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/**
 * Common Database collection queries
 */
public class BaseCollection<T extends BaseModel> {

    private final MongoCollection<T> collection;

    /**
     * Construct a new BaseCollection
     *
     * @param collectionName Collection name
     * @param cls            Model class
     */
    public BaseCollection(String collectionName, Class<T> cls) {
        collection = Database.getCollection(collectionName, cls);
    }

    /**
     * Get the underlying Mongo Collection
     *
     * @return underlying Mongo Collection
     */
    public MongoCollection<T> getCollection() {
        return collection;
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
        getCollection().insertOne(obj);
    }

    /**
     * Get a document by its UUID
     *
     * @param id UUID
     * @return POJO
     */
    public T getById(UUID id) {
        return getCollection().find(eq("_id", id)).first();
    }
}
