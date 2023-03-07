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
     * @param email The Auth ID of the user.
     * @return The POJO.
     */
    public User getUserByEmail(String email) {
        return this.getCollection().find(this.emailFilter(email)).first();
    }


    public User getOrCreateUserByEmail(String email) {
        User user = this.getUserByUserId(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            this.insert(user);
        }
        return user;
    }

    private Bson userIdFilter(String userId) {
        return eq(userId);
    }

    private Bson emailFilter(String email) {
        return eq("email", email);
    }
}
