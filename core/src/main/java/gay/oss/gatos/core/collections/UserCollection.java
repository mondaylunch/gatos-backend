package gay.oss.gatos.core.collections;

import gay.oss.gatos.core.models.User;

import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.eq;

/**
 * "users" collection for {@link User}
 */
public class UserCollection extends BaseCollection<User> {

    public UserCollection() {
        super("users", User.class);
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
     * Gets a document.
     *
     * @param username The username of the user.
     * @return The POJO.
     */
    public User getUser(String username) {
        return getCollection().find(usernameFilter(username)).first();
    }

}
