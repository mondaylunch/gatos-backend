package gay.oss.gatos.core.models;

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
     * @param String password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
