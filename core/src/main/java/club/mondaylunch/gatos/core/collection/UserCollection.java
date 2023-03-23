package club.mondaylunch.gatos.core.collection;

import java.util.UUID;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;

import club.mondaylunch.gatos.core.models.User;
import club.mondaylunch.gatos.core.models.UserData;

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
        return this.getCollection().find(Filters.eq(userId)).first();
    }

    /**
     * Gets a document.
     *
     * @param email The Auth ID of the user.
     * @return The POJO.
     */
    public User getUserByEmail(String email) {
        return this.getCollection().find(Filters.eq("email", email)).first();
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

    @Override
    public void delete(UUID id) {
        super.delete(id);
        UserData.objects.delete(id);
    }
}
