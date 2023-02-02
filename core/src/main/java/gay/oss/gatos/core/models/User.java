package gay.oss.gatos.core.models;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import gay.oss.gatos.core.collection.UserCollection;

/**
 * POJO for users.
 */
public class User extends BaseModel {

    public static final UserCollection objects = new UserCollection();

    private String username;
    private String email;
    private String password;

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
     * Set the username.
     *
     * @param String username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the email.
     *
     * @param String email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set the password.
     *
     * @param String hashed password
     */
    public void setPassword(String passwordHash) {
        this.password = passwordHash;
    }

    /**
     * Hash and set the password.
     *
     * @param String password
     */
    public void hashPlaintextPassword(String plaintextPassword) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String hash = encoder.encode(plaintextPassword);
        this.password = hash;
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
}
