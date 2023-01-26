package gay.oss.gatos.core.models;

import org.bson.codecs.pojo.annotations.BsonIgnore;
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
    @BsonIgnore
    private Argon2 argon2;
    @BsonIgnore
    private String rawPassword;

    public User() {
        this.argon2 = this.getArgon2Instance();
    }

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
     * Get the raw password for authentication purposes.
     *
     * @return display the raw password.
     */
    public String getRawPassword() {
        return this.rawPassword;
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
        this.password = this.argon2.hash(4, 65586, 2, password.toCharArray());
        this.argon2.wipeArray(password.toCharArray());
    }

    /**
     * Set the raw password field.
     *
     * @param String rawPassword
     */
    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    /**
     * compare what is in the database with the actual password.
     *
     * @param String rawPassword
     */
    public boolean comparePassword(String rawPassword) {
        return this.argon2.verify(this.getPassword(), rawPassword.toCharArray());
    }

    /**
     * get an argon2 instance.
     *
     * @return an Argon2 instance
     */
    private Argon2 getArgon2Instance() {
        return Argon2Factory.create();
    }
}
