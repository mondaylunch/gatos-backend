package club.mondaylunch.gatos.api.helpers;

import java.util.UUID;

import club.mondaylunch.gatos.core.models.User;

public interface UserCreationHelper {
    /**
     * Create a new user with given properties.
     * @param username Username
     * @param email    Email
     * @return New user
     */
    default User createSimpleUser(String username, String email) {
        var user = new User();
        user.setEmail(email);
        User.objects.insert(user);
        return user;
    }

    /**
     * Create a new user with random values.
     */
    default User createRandomUser() {
        return this.createSimpleUser(UUID.randomUUID().toString(), UUID.randomUUID() + "@example.com");
    }
}
