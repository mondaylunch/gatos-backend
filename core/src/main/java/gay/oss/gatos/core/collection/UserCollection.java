package gay.oss.gatos.core.collection;

import java.util.UUID;

import gay.oss.gatos.core.models.User;

/**
 * "users" collection for {@link User}.
 */
public class UserCollection extends BaseCollection<User> {
    public UserCollection() {
        super("users", User.class);
    }

    public User authenticate(String token) throws RuntimeException {
        User user = new User();
        user.setId(new UUID(0, 0));

        return user;
    }
}