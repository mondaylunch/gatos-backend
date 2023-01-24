package gay.oss.gatos.core.collections;

import com.mongodb.client.MongoCollection;
import gay.oss.gatos.core.Database;
import gay.oss.gatos.core.models.BaseModel;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/**
 * Common Database collection queries.
 */
public class BaseCollection<T extends BaseModel> {

    private final MongoCollection<T> collection;

    /**
     * Construct a new BaseCollection.
     *
     * @param collectionName Collection name.
     * @param cls            Model class.
     */
    public BaseCollection(String collectionName, Class<T> cls) {
        collection = Database.getCollection(collectionName, cls);
    }

    /**
     * Get the underlying Mongo Collection.
     *
     * @return underlying Mongo Collection.
     */
    public MongoCollection<T> getCollection() {
        return collection;
    }

    /**
     * Insert a new document into the collection.
     *
     * @param obj The POJO.
     */
    public void insert(T obj) {
        if (obj.getId() == null) {
            obj.setId(UUID.randomUUID());
        }
        getCollection().insertOne(obj);
    }

    /**
     * Gets a document.
     *
     * @param id The ID of the document to get.
     * @return The POJO.
     */
    public T get(UUID id) {
        return getCollection().find(idFilter(id)).first();
    }

    /**
     * Get documents by a field.
     *
     * @param field The field name.
     * @param value The field value.
     * @return a {@code List} of POJOs.
     */
    public List<T> get(String field, Object value) {
        List<T> list = new ArrayList<>();
        for (T obj : getCollection().find(eq(field, value))) {
            list.add(obj);
        }
        return list;
    }

    /**
     * Creates an ID filter.
     *
     * @param id The ID to filter by.
     * @return The filter.
     */
    private static Bson idFilter(UUID id) {
        return eq("_id", id);
    }
}
