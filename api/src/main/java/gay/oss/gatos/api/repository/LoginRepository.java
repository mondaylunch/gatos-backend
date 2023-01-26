package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.core.models.User;

@Repository
public class LoginRepository {

    /**
     * Validate user.
     *
     * @param String username
     * @param String password
     */
    public User validateUser(String username, String password) throws UserNotFoundException {
        User usr = User.objects.getUser(username);
        if (usr == null) {
            throw new UserNotFoundException();
        } else {
            if (usr.comparePassword(password)) {
                return usr;
            }
            throw new UserNotFoundException();
        }
    }
}
