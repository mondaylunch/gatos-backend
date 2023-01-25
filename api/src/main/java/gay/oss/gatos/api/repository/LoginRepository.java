package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.core.models.User;

@Repository
public class LoginRepository {

    /**
     * Validate user.
     * TODO change after hashing the password
     *
     * @param String username
     * @param String password
     */
    public User validateUser(String username, String password) throws UserNotFoundException {
        // get the user first
        User usr = User.objects.getUser(username);
        if (usr.getPassword().equals(password)) {
            return usr;
        }
        throw new UserNotFoundException();
    }

}
