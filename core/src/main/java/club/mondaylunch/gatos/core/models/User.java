package club.mondaylunch.gatos.core.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.collection.UserCollection;

/**
 * POJO for users.
 */
public class User extends BaseModel {
    public static final UserCollection objects = new UserCollection();

    @BsonProperty("email")
    @JsonProperty("email")
    private String email;

    @BsonProperty("discord_id")
    @JsonProperty("discord_id")
    private @Nullable String discordId;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public @Nullable String getDiscordId() {
        return this.discordId;
    }

    public void setDiscordId(@Nullable String discordId) {
        this.discordId = discordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(this.getEmail(), user.getEmail()) && Objects.equals(this.getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getEmail(), this.getId());
    }
}
