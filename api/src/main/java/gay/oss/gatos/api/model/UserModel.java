package gay.oss.gatos.api.model;

/*
 * definition of the user model that will be stored in the database
 */
public class UserModel {
    
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;

    // getters for the fields of the user
    public String getID(){
        return id;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    // define setters for the fields
    public void setID(String id){
        this.id = id;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    // to string method
    @Override
    public String toString(){
        return "user " + this.firstName + " " + this.lastName + " \n"
                + "id: " + this.id + " \n"
                + "username: " + this.username + " \n"
                + "email: " + this.email + " \n"
                + "password: " + this.password;
    }
}
