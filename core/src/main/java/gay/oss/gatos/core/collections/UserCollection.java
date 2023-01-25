package gay.oss.gatos.core.collections;

import java.util.UUID;

import gay.oss.gatos.core.models.User;

import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.eq;

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
     * checks if the username is already in use
     *
     * @param username The potential username of the user.
     * @return true if the username is already in use
     */
    public Boolean usernameAlreadyInUse(String username){
        return getCollection().find(usernameFilter(username)).first() != null;
    }

    /**
     * checks if the email is already in use
     *
     * @param email The potential email of the user.
     * @return true if the email is already in use
     */
    public Boolean emailAlreadyInUse(String username){
        return getCollection().find(emailFilter(username)).first() != null;
    }

    public User authenticate(String token) throws RuntimeException {
        User user = new User();
        user.setId(new UUID(0, 0));

        return user;
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
