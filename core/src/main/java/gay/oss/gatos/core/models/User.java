package gay.oss.gatos.core.models;

import org.bson.codecs.pojo.annotations.BsonProperty;
import gay.oss.gatos.core.collections.UserCollection;

/**
 * POJO for users
 */
public class User extends BaseModel {

    public static UserCollection objects = new UserCollection();

    @BsonProperty("first_name")
    private String firstName;
    @BsonProperty("last_name")
    private String lastName;
    private String username;
    private String email;
    private String password;

    /**
     * Get the first name
     *
     * @return display first name
     */
    public String getFirstName(){
        return firstName;
    }

    /**
     * Get the last name
     *
     * @return display last name
     */
    public String getLastName(){
        return lastName;
    }

    /**
     * Get the username
     *
     * @return display username
     */
    public String getUsername(){
        return username;
    }

    /**
     * Get the email
     *
     * @return display email
     */
    public String getEmail(){
        return email;
    }

    /**
     * Get the password
     *
     * @return display password
     */
    public String getPassword(){
        return password;
    }

    /**
     * Set the first name
     *
     * @param String firstName
     */
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    /**
     * Set the last name
     *
     * @param String lastName
     */
    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    /**
     * Set the username
     *
     * @param String username
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Set the email
     *
     * @param String email
     */
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * Set the password
     *
     * @param String password
     */
    public void setPassword(String password){
        this.password = password;
    }
}
