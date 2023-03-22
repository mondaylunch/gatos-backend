package club.mondaylunch.gatos.core.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.collection.FlowDataCollection;
import club.mondaylunch.gatos.core.data.DataBox;

public class FlowData {

    public static final FlowDataCollection objects = new FlowDataCollection();

    @BsonId
    @BsonProperty("_id")
    @JsonProperty("_id")
    private Id id;

    private DataBox<?> value;

    @SuppressWarnings("unused")
    public FlowData() {
    }

    public FlowData(UUID flowId, String key, DataBox<?> value) {
        this.id = new Id(flowId, key);
        this.value = value;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public DataBox<?> getValue() {
        return value;
    }

    public void setValue(DataBox<?> value) {
        this.value = value;
    }

    public record Id(
        @BsonProperty("flow_id")
        @JsonProperty("flow_id")
        UUID flowID,
        String key
    ) {
    }
}