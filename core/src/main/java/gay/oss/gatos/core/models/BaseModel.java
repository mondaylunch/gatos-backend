package gay.oss.gatos.core.models;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * Class describing common properties for all database models.
 */
public class BaseModel {

    @BsonId
    @BsonProperty("_id")
    private UUID id;

    /**
     * Get model's UUID.
     *
     * @return UUID
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Set model's UUID.
     *
     * @param uuid UUID
     */
    public void setId(UUID uuid) {
        this.id = uuid;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BaseModel other) {
            return this.id.equals(other.id);
        } else {
            return false;
        }
    }
}
