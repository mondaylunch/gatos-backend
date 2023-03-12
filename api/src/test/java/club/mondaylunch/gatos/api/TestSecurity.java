package club.mondaylunch.gatos.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;

// https://stackoverflow.com/a/61790559
public class TestSecurity {
    public static final String FAKE_TOKEN = "token";

    public static Jwt jwt() {
        Map<String, Object> claims = Map.of(
            "sub", "user"
        );

        return new Jwt(
            FAKE_TOKEN,
            Instant.now(),
            Instant.now().plusSeconds(30),
            Map.of("alg", "none"),
            claims
        );
    }
}
