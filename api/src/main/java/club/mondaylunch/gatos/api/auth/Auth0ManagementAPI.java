package club.mondaylunch.gatos.api.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages communicating with the Auth0 Management API.
 */
@Component
public class Auth0ManagementAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0ManagementAPI.class);
    private static final String USER_AGENT = "Gatos/1.0";
    private static final String TOKEN_REQUEST_URL;
    private static final String CLIENT_ID;
    private static final String CLIENT_SECRET;
    private static final String AUDIENCE;
    static {
        String path = "auth0management.properties";
        Properties properties = new Properties();
        try (InputStream stream = Auth0ManagementAPI.class.getClassLoader().getResourceAsStream(path)) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + path, e);
        }
        TOKEN_REQUEST_URL = properties.getProperty("token_request_url");
        CLIENT_ID = properties.getProperty("client_id");
        CLIENT_SECRET = properties.getProperty("client_secret");
        AUDIENCE = properties.getProperty("audience");
    }

    private static final HttpClient CLIENT = HttpClient.newBuilder().build();

    private static String token;
    private static Instant tokenExpiration;

    /**
     * Gets a token for the Auth0 Management API, requesting a new one if necessary.
     * @return a token
     */
    private static String getToken() {
        if (token == null || tokenExpiration == null || Instant.now().isAfter(tokenExpiration)) {
            LOGGER.info("Requesting a new Auth0 Management API token");
            var req = HttpRequest.newBuilder(URI.create(TOKEN_REQUEST_URL))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "grant_type=client_credentials&client_id=%s&client_secret=%s&audience=%s"
                        .formatted(CLIENT_ID, CLIENT_SECRET, AUDIENCE)))
                .build();
            HttpResponse<String> response;
            try {
                response = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to get Auth0 Management API token: ", e);
            }

            if (response.statusCode() != 200 || response.body() == null || response.body().isEmpty()) {
                throw new RuntimeException("Failed to get Auth0 Management API token: token request failed with %s response: %s".formatted(response.statusCode(), response.body()));
            }

            try {
                var responseJson = new Gson().fromJson(response.body(), new TypeToken<Map<String, Object>>() {});
                token = (String) responseJson.get("access_token");
                tokenExpiration = Instant.now().plusSeconds(((Number) responseJson.get("expires_in")).intValue());
            } catch (JsonSyntaxException e) {
                throw new RuntimeException("Failed to get Auth0 Management API token: token request returned invalid JSON: " + response.body(), e);
            }
        }
        return token;
    }

    /**
     * Makes a GET request to the Auth0 Management API.
     * @param url the URL to send the request to
     * @return    the response body
     */
    private static String get(String url) {
        try {
            var req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .build();
            return CLIENT.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get Auth0 Management API resource: " + url, e);
        }
    }

    /**
     * Gets the user profile for the given user ID as a JSON Object.
     * @param userId the user ID
     * @return       the user profile
     */
    public static JsonObject getUserProfile(String userId) {
        return new Gson().fromJson(get(AUDIENCE + "users/" + URLEncoder.encode(userId, StandardCharsets.UTF_8)), JsonObject.class);
    }
}
