package club.mondaylunch.gatos.core.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import club.mondaylunch.gatos.core.collection.UserCollection;

/**
 * POJO for users.
 */
public class User extends BaseModel {

    public static final UserCollection objects = new UserCollection();

    private String username;
    private String email;
    private String password;
    @BsonProperty("auth_token")
    @JsonProperty("auth_token")
    private String authToken;

    /**
     * Get the username.
     *
     * @return display username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get the email.
     *
     * @return display email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Get the password.
     *
     * @return display password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get the auth token.
     *
     * @return Auth token
     */
    public String getAuthToken() {
        return this.authToken;
    }

    /**
     * Set the username.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the email.
     *
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set the password.
     *
     * @param passwordHash The password hash
     */
    public void setPassword(String passwordHash) {
        this.password = passwordHash;
    }

    /**
     * Set the auth token.
     *
     * @param authToken The authentication token
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    /**
     * Hash and set the password.
     *
     * @param plaintextPassword The plaintext password
     */
    public void hashPlaintextPassword(String plaintextPassword) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.password = encoder.encode(plaintextPassword);
    }

    /**
     * Check whether a given plaintext password is correct for the stored hash.
     *
     * @param plaintextPassword Plaintext password
     * @return Whether it is valid
     */
    public boolean validatePassword(String plaintextPassword) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return encoder.matches(plaintextPassword, this.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.username, this.email, this.password, this.authToken);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else if (!super.equals(obj)) {
            return false;
        } else {
            var other = (User) obj;
            return Objects.equals(this.username, other.username)
                && Objects.equals(this.email, other.email)
                && Objects.equals(this.password, other.password)
                && Objects.equals(this.authToken, other.authToken);
        }
    }
}
