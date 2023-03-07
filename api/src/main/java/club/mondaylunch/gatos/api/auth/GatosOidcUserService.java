package club.mondaylunch.gatos.api.auth;

import com.google.gson.JsonObject;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import club.mondaylunch.gatos.core.models.User;

public class GatosOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final OidcUserService oidcUserService = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = this.oidcUserService.loadUser(userRequest);
        User user = this.getOrCreateUser(oidcUser.getClaimAsString("email"));
        this.updateUserWithProfileData(user, Auth0ManagementAPI.getUserProfile(oidcUser.getClaimAsString("sub")));
        return new GatosOidcUser(oidcUser, user);
    }

    private void updateUserWithProfileData(User user, JsonObject userProfile) {
        if (userProfile.has("identities")) {
            var identities = userProfile.getAsJsonArray("identities");
            for (var identity : identities) {
                var identityObject = identity.getAsJsonObject();
                if (identityObject.has("connection")
                    && identityObject.get("connection").getAsString().equals("discord")) {
                    String unsplitDiscordId = identityObject.get("user_id").getAsString();
                    String discordId = unsplitDiscordId.split("\\|")[1];
                    this.updateDiscordId(user, discordId);
                }
            }
        }
    }

    /**
     * Gets or creates a user object with a given email.
     *
     * @param email the email
     * @return a user object
     */
    public User getOrCreateUser(String email) {
        return User.objects.getOrCreateUserByEmail(email);
    }

    /**
     * Updates the discord id of a user.
     * @param user      the user
     * @param discordId the discord id
     */
    public void updateDiscordId(User user, String discordId) {
        user.setDiscordId(discordId);
        User.objects.update(user.getId(), user);
    }
}
