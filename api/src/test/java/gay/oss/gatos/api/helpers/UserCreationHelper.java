package gay.oss.gatos.api.helpers;

import gay.oss.gatos.core.models.User;

public interface UserCreationHelper {
    static String DEFAULT_USERNAME = "RealPerson";
    static String DEFAULT_EMAIL = "jeroenisthebest@example.com";
    static String DEFAULT_PASSWORD = "Kolling2021";

    /**
     * Create a new user with given properties
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
        User.objects.insert(user);
        return user;
    }

    /**
     * Create a new user with default values
     */
    default User createDefaultUser() {
        return createSimpleUser(DEFAULT_USERNAME, DEFAULT_EMAIL);
    }
}
