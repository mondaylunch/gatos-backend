package club.mondaylunch.gatos.api.auth;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import club.mondaylunch.gatos.core.models.User;

public class GatosOidcUser implements OidcUser {
    private final OidcUser wrapped;
    private final User user;

    public GatosOidcUser(OidcUser wrapped, User user) {
        this.wrapped = wrapped;
        this.user = user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.wrapped.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return this.wrapped.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return this.wrapped.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.wrapped.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.wrapped.getAuthorities();
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    public User getUser() {
        return this.user;
    }
}
