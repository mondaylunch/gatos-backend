package club.mondaylunch.gatos.api.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final LogoutHandler logoutHandler;

    public SecurityConfig(LogoutHandler logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
            // allow all users to access the home pages and the static images directory
            .requestMatchers("/api/v1/login/is-logged-in").permitAll()
            // all other requests must be authenticated
            .anyRequest().authenticated()
            .and().oauth2Login()
                .userInfoEndpoint().oidcUserService(new GatosOidcUserService()).and()
            .and().logout()
            // handle logout requests at /logout path
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            // customize logout handler to log out of Auth0
            .addLogoutHandler(this.logoutHandler);
        return http.build();
    }
}
