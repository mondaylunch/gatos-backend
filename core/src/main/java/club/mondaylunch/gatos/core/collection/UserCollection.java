package club.mondaylunch.gatos.core.collection;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.JsonObject;
import org.bson.conversions.Bson;

import club.mondaylunch.gatos.core.models.User;

/**
 * "users" collection for {@link User}.
 */
public class UserCollection extends BaseCollection<User> {

    public UserCollection() {
        super("users", User.class);
    }

    /**
     * Gets a document.
     *
     * @param userId The ID of the user.
     * @return The POJO.
     */
    public User getUserByUserId(String userId) {
        return this.getCollection().find(this.userIdFilter(userId)).first();
    }

    /**
     * Gets a document.
     *
     * @param email The Auth ID of the user.
     * @return The POJO.
     */
    public User getUserByEmail(String email) {
        return this.getCollection().find(this.emailFilter(email)).first();
    }

    public User getOrCreateUserByEmail(String email) {
        User user = this.getUserByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            this.insert(user);
        }
        return user;
    }

    private void updateUserWithProfileData(User user, JsonObject userProfile) {
        if (userProfile.has("identities")) {
            var identities = userProfile.getAsJsonArray("identities");
            for (var identity : identities) {
                var identityObject = identity.getAsJsonObject();
                if (identityObject.has("connection")
                    && identityObject.get("connection").getAsString().equals("discord")) {
                    String unsplitDiscordId = identityObject.get("user_id").getAsString();
                    String discordId = unsplitDiscordId.split("\\|")[1];
                    user.setDiscordId(discordId);
                }
            }
        }
        this.update(user.getId(), user);
    }

    public User updateDetailsForUser(User user, JsonObject userProfile) {
        this.updateUserWithProfileData(user, userProfile);
        return user;
    }

    private Bson userIdFilter(String userId) {
        return eq(userId);
    }

    private Bson emailFilter(String email) {
        return eq("email", email);
    }
}
