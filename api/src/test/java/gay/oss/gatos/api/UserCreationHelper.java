package gay.oss.gatos.api;

import gay.oss.gatos.core.models.User;

public interface UserCreationHelper {
    String DEFAULT_USERNAME = "RealPerson";
    String DEFAULT_EMAIL = "jeroenisthebest@example.com";
    String DEFAULT_PASSWORD = "Kolling2021";

    static User createSimpleUser(String username, String email) {
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(DEFAULT_PASSWORD);
        return user;
    }

    static User createDefaultUser() {
        return createSimpleUser(DEFAULT_USERNAME, DEFAULT_EMAIL);
    }

    default void clearUserDatabase() {
        User.objects.clear();
    }
}
