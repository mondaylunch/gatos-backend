package gay.oss.gatos.core.models;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import gay.oss.gatos.core.collections.UserCollection;

/**
 * POJO for users.
 */
public class User extends BaseModel {

    public static UserCollection objects = new UserCollection();

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
     * Set the password field to a hashed password using argon2.
     *
     * @param String password
     */
    public void setPassword(String password) {
        Argon2 argon2 = this.getArgon2Instance();
        this.password = argon2.hash(4, 65586, 2, password.toCharArray());
    }

    public boolean comparePassword(String password) {
        Argon2 argon2 = this.getArgon2Instance();
        return argon2.verify(this.getPassword(), password.toCharArray());
    }

    private Argon2 getArgon2Instance() {
        return Argon2Factory.create();
    }
}
