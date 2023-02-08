package gay.oss.gatos.core.collection;

import static com.mongodb.client.model.Filters.eq;

import org.bson.conversions.Bson;

import gay.oss.gatos.core.models.User;

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
     * @param username The username of the user.
     * @return The POJO.
     */
    public User getUser(String username) {
        return getCollection().find(usernameFilter(username)).first();
    }

    /**
     * Gets a document.
     *
     * @param email The email of the user.
     * @return The POJO.
     */
    public User getUserByEmail(String email) {
        return getCollection().find(emailFilter(email)).first();
    }

    /**
     * Gets a document.
     *
     * @param authToken User's auth token.
     * @return The POJO.
     */
    public User getUserByToken(String authToken) {
        return getCollection().find(eq("auth_token", authToken)).first();
    }

    /**
     * checks if the username is already in use.
     *
     * @param username The potential username of the user.
     * @return true if the username is already in use
     */
    public Boolean usernameAlreadyInUse(String username) {
        return getCollection().find(usernameFilter(username)).first() != null;
    }

    /**
     * checks if the email is already in use.
     *
     * @param email The potential email of the user.
     * @return true if the email is already in use
     */
    public Boolean emailAlreadyInUse(String email) {
        return getCollection().find(emailFilter(email)).first() != null;
    }

    /**
     * Creates a username filter.
     *
     * @param String The username to filter by.
     * @return The filter.
     */
    private static Bson usernameFilter(String username) {
        return eq("username", username);
    }

    /**
     * Creates an email filter.
     *
     * @param String The email to filter by.
     * @return The filter.
     */
    private static Bson emailFilter(String email) {
        return eq("email", email);
    }
}
