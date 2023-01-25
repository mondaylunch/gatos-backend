package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;
import gay.oss.gatos.core.models.User;

@Repository
public class SignUpRepository {

    /**
     * add a new user to the db
     *
     * @param User user
     */
    public User addUser(User user) {
        // validate if we can add the user
        if (usernameAlreadyInUse(user.getUsername()) || emailAlreadyInUse(user.getEmail())) {
            // probably return some error :thinkin:
        }else{
            User.objects.insert(user);
        }
        return user;
    }


    /**
     * check if the username is already in use
     *
     * @param String username
     * @return true if the username is already in use
     */
    public Boolean usernameAlreadyInUse(String username){
        return User.objects.usernameAlreadyInUse(username);
    }

    /**
     * checks if the email is already in use
     *
     * @param String email
     * @return true if the email is already in use
     */
    public Boolean emailAlreadyInUse(String email){
        return User.objects.emailAlreadyInUse(email);
    }

}
