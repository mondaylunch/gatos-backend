package club.mondaylunch.gatos.api.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manages communicating with the Auth0 Management API.
 */
@Component
public class Auth0ManagementAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0ManagementAPI.class);
    private static final String USER_AGENT = "Gatos/1.0";
    @Value("${auth0.management.token_request_url}")
    private String tokenRequestUrl;
    @Value("${auth0.management.client_id}")
    private String clientId;
    @Value("${auth0.management.client_secret}")
    private String clientSecret;
    @Value("${auth0.management.audience}")
    private String audience;

    private final HttpClient client = HttpClient.newBuilder().build();

    private String token;
    private Instant tokenExpiration;

    /**
     * Gets a token for the Auth0 Management API, requesting a new one if necessary.
     * @return a token
     */
    private String getToken() {
        if (this.token == null || this.tokenExpiration == null || Instant.now().isAfter(this.tokenExpiration)) {
            LOGGER.info("Requesting a new Auth0 Management API token");
            var req = HttpRequest.newBuilder(URI.create(this.tokenRequestUrl))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "grant_type=client_credentials&client_id=%s&client_secret=%s&audience=%s"
                        .formatted(this.clientId, this.clientSecret, this.audience)))
                .build();
            HttpResponse<String> response;
            try {
                response = this.client.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to get Auth0 Management API token: ", e);
            }

            if (response.statusCode() != 200 || response.body() == null || response.body().isEmpty()) {
                throw new RuntimeException("Failed to get Auth0 Management API token: token request failed with %s response: %s".formatted(response.statusCode(), response.body()));
            }

            try {
                var responseJson = new Gson().fromJson(response.body(), new TypeToken<Map<String, Object>>() {});
                this.token = (String) responseJson.get("access_token");
                this.tokenExpiration = Instant.now().plusSeconds(((Number) responseJson.get("expires_in")).intValue());
            } catch (JsonSyntaxException e) {
                throw new RuntimeException("Failed to get Auth0 Management API token: token request returned invalid JSON: " + response.body(), e);
            }
        }
        return this.token;
    }

    /**
     * Makes a GET request to the Auth0 Management API.
     * @param url the URL to send the request to
     * @return    the response body
     */
    private String get(String url) {
        try {
            var req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + this.getToken())
                .GET()
                .build();
            return this.client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get Auth0 Management API resource: " + url, e);
        }
    }

    /**
     * Gets the user profile for the given user ID as a JSON Object.
     * @param userId the user ID
     * @return       the user profile
     */
    public JsonObject getUserProfile(String userId) {
        try {
            JsonArray array = new Gson().fromJson(this.get(this.audience + "users-by-email?email=" + URLEncoder.encode(userId, StandardCharsets.UTF_8)), JsonArray.class);
            if (array.size() == 0 || !array.get(0).isJsonObject()) {
                return new JsonObject();
            } else {
                return array.get(0).getAsJsonObject();
            }
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to get user profile for {}: invalid JSON returned", userId, e);
            return new JsonObject();
        }
    }
}
