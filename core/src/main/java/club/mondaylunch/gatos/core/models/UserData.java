package club.mondaylunch.gatos.core.models;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.collection.UserDataCollection;
import club.mondaylunch.gatos.core.data.DataBox;

public class UserData {

    public static final UserDataCollection objects = new UserDataCollection();

    @BsonId
    @BsonProperty("_id")
    @JsonProperty("_id")
    private Id id;

    private DataBox<?> value;

    @SuppressWarnings("unused")
    public UserData() {
    }

    public UserData(UUID userId, String key, DataBox<?> value) {
        this.id = new Id(userId, key);
        this.value = value;
    }

    public Id getId() {
        return this.id;
    }

    @SuppressWarnings("unused")
    public void setId(Id id) {
        this.id = id;
    }

    public DataBox<?> getValue() {
        return this.value;
    }

    @SuppressWarnings("unused")
    public void setValue(DataBox<?> value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            UserData userData = (UserData) obj;
            return Objects.equals(this.id, userData.id) && Objects.equals(this.value, userData.value);
        }
    }

    @Override
    public String toString() {
        return "UserData{"
            + "id=" + this.id
            + ", value=" + this.value
            + '}';
    }

    public record Id(
        @BsonProperty("user_id")
        @JsonProperty("user_id")
        UUID userId,
        String key
    ) {
    }
}
