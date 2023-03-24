package club.mondaylunch.gatos.api.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import club.mondaylunch.gatos.api.auth.Auth0ManagementAPI;
import club.mondaylunch.gatos.core.Environment;
import club.mondaylunch.gatos.core.models.User;

@Repository
public class UserRepository {
    private final Auth0ManagementAPI auth0ManagementAPI;

    @Autowired
    public UserRepository(Auth0ManagementAPI auth0ManagementAPI) {
        this.auth0ManagementAPI = auth0ManagementAPI;
    }

    /**
     * Gets or creates a user object with a given email.
     *
     * @param email the email
     * @return a user object
     */
    public User getOrCreateUser(String email) {
        var res = User.objects.getOrCreateUserByEmail(email);
        if (res.getDiscordId() == null && !Environment.isJUnitTest()) {
            return this.getUserWithUpdatedDetails(res);
        } else {
            return res;
        }
    }

    /**
     * Ensures the details of a user are complete.
     * @param user      the user
     */
    public User getUserWithUpdatedDetails(User user) {
        return User.objects.updateDetailsForUser(user, this.auth0ManagementAPI.getUserProfile(user.getEmail()));
    }
}
