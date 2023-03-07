package club.mondaylunch.gatos.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.collection.UserCollection;

/**
 * POJO for users.
 */
public class User extends BaseModel {
    public static final UserCollection objects = new UserCollection();

    @BsonProperty("user_name")
    @JsonProperty("user_id")
    private String authId;

    public String getAuthId() {
        return this.authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }
}
