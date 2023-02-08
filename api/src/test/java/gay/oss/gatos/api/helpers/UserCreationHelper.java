package gay.oss.gatos.api.helpers;

import java.util.UUID;

import gay.oss.gatos.core.models.User;

public interface UserCreationHelper {
    String DEFAULT_PASSWORD = "Kolling2021";

    /**
     * Create a new user with given properties.
     * 
     * @param username Username
     * @param email    Email
     * @return New user
     */
    default User createSimpleUser(String username, String email) {
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.hashPlaintextPassword(DEFAULT_PASSWORD);
        user.setAuthToken(UUID.randomUUID().toString());
        User.objects.insert(user);
        return user;
    }

    /**
     * Create a new user with random values.
     */
    default User createRandomUser() {
        return this.createSimpleUser(UUID.randomUUID().toString(), UUID.randomUUID().toString() + "@example.com");
    }
}
