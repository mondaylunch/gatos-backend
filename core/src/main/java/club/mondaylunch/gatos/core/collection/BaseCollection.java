package club.mondaylunch.gatos.core.collection;

import static com.mongodb.client.model.Filters.eq;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.Database;
import club.mondaylunch.gatos.core.models.BaseModel;

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
        this.collection = Database.getCollection(collectionName, cls);
    }

    /**
     * Get the underlying Mongo Collection.
     *
     * @return underlying Mongo Collection.
     */
    public MongoCollection<T> getCollection() {
        return this.collection;
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
        obj.beforeSerialization();
        this.getCollection().insertOne(obj);
    }

    /**
     * Gets a document.
     *
     * @param id The ID of the document to get.
     * @return The POJO.
     */
    public T get(UUID id) {
        T object = this.getCollection().find(idFilter(id)).first();
        if (object != null) {
            object.afterDeserialization();
        }
        return object;
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
        for (T obj : this.getCollection().find(eq(field, value))) {
            obj.afterDeserialization();
            list.add(obj);
        }
        return list;
    }

    /**
     * Updates a document. Only non-null fields will be updated.
     * The ID field cannot be updated.
     *
     * @param id  The ID of the document to update.
     * @param obj The POJO to update with.
     * @return The updated POJO.
     */
    public T update(UUID id, T obj) {
        List<Bson> updates = getNonNullUpdates(obj);
        if (!updates.isEmpty()) {
            this.getCollection().updateOne(idFilter(id), Updates.combine(updates));
        }
        return this.get(id);
    }

    /**
     * Deletes a document.
     *
     * @param id The ID of the document to delete.
     */
    public void delete(UUID id) {
        this.getCollection().deleteOne(idFilter(id));
    }

    /**
     * Deletes all documents.
     */
    public void clear() {
        this.getCollection().drop();
    }

    /**
     * Gets the size of the collection.
     *
     * @return The size of the collection.
     */
    public long size() {
        return this.getCollection().countDocuments();
    }

    /**
     * Creates an ID filter.
     *
     * @param id The ID to filter by.
     * @return The filter.
     */
    protected static Bson idFilter(UUID id) {
        return eq("_id", id);
    }

    /**
     * Creates a list of non-null updates from a POJO.
     *
     * @param obj The POJO.
     * @return A list of updates.
     */
    private static List<Bson> getNonNullUpdates(Object obj) {
        return createPropertyDescriptorStream(obj.getClass())
            .filter(BaseCollection::hasGetter)
            .map(descriptor -> getField(descriptor, obj))
            .filter(Objects::nonNull)
            .map(BaseCollection::createUpdate)
            .toList();
    }

    /**
     * Creates a stream of {@code PropertyDescriptors} from a class. The
     * {@link Object} class is excluded.
     *
     * @param clazz The class.
     * @return A stream of {@code PropertyDescriptors}.
     */
    private static Stream<PropertyDescriptor> createPropertyDescriptorStream(Class<?> clazz) {
        try {
            return Stream.of(Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a {@code PropertyDescriptor} has a getter. The {@code id}
     * field is excluded.
     *
     * @param descriptor The {@code PropertyDescriptor}.
     * @return {@code true} if the {@code PropertyDescriptor} has a getter,
     * {@code false} otherwise.
     */
    private static boolean hasGetter(PropertyDescriptor descriptor) {
        return descriptor.getReadMethod() != null && !descriptor.getName().equals("id");
    }

    /**
     * Creates a representation of an object field by
     * invoking the getter of a {@code PropertyDescriptor}
     * on the passed object.
     *
     * @param descriptor The {@code PropertyDescriptor}.
     * @param obj        The object to invoke the getter on.
     * @return An {@code Entry} representing the object field.
     */
    @Nullable
    private static Map.Entry<String, Object> getField(PropertyDescriptor descriptor, Object obj) {
        try {
            Object value = descriptor.getReadMethod().invoke(obj);
            if (value != null) {
                // Assume the name is the exact name we want
                String name = descriptor.getName();

                // 🙂 reflection time! 🙂
                // Find the field we are dealing with
                Field field = obj.getClass().getDeclaredField(descriptor.getName());

                // Get BsonProperty annotations
                BsonProperty[] properties = field.getDeclaredAnnotationsByType(BsonProperty.class);

                // Apply correct name if it exists
                if (properties.length > 0) {
                    name = properties[0].value();
                }

                return Map.entry(name, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a MongoDB update.
     *
     * @param entry The entry to create an update from.
     * @return The update.
     */
    private static Bson createUpdate(Map.Entry<String, Object> entry) {
        return Updates.set(entry.getKey(), entry.getValue());
    }
}
