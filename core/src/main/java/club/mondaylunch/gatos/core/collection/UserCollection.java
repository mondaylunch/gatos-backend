package club.mondaylunch.gatos.core.collection;

import static com.mongodb.client.model.Filters.eq;

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
     * @param authId The Auth ID of the user.
     * @return The POJO.
     */
    public User getUserByAuthId(String authId) {
        return this.getCollection().find(this.authIdFilter(authId)).first();
    }


    public User getOrCreateUserByAuthId(String authId) {
        User user = this.getUserByUserId(authId);
        if (user == null) {
            user = new User();
            user.setAuthId(authId);
            this.getCollection().insertOne(user);
        }
        return user;
    }

    private Bson userIdFilter(String userId) {
        return eq("user_id", userId);
    }

    private Bson authIdFilter(String authId) {
        return eq("auth_id", authId);
    }
}
