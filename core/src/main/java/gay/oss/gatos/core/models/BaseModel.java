package gay.oss.gatos.core.models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

/**
 * Class describing common properties for all database models
 */
public class BaseModel {

    @BsonId
    @BsonProperty("_id")
    private UUID id;

    /**
     * Get model's UUID
     *
     * @return UUID
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Set model's UUID
     *
     * @param uuid UUID
     */
    public void setId(UUID uuid) {
        this.id = uuid;
    }
}
