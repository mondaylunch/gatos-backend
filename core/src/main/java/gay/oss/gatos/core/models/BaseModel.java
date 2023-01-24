package gay.oss.gatos.core.models;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class BaseModel {
    @BsonId
    @BsonProperty("_id")
    private UUID id;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }
}
