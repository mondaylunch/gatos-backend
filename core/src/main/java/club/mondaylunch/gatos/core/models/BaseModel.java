package club.mondaylunch.gatos.core.models;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.codec.SerializationUtils;

/**
 * Class describing common properties for all database models.
 */
public class BaseModel {

    @BsonId
    @BsonProperty("_id")
    @JsonProperty("_id")
    private UUID id;

    public BaseModel(UUID id) {
        this.id = id;
    }

    public BaseModel() {
    }

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

    public String toJson() {
        return SerializationUtils.toJson(this);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            var other = (BaseModel) obj;
            return Objects.equals(this.id, other.id);
        }
    }
}
