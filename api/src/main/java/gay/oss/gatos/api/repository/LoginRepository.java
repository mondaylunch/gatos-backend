package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.core.models.User;

@Repository
public class LoginRepository {

    /**
     * Authenticate the user.
     *
     * @param User the user we want to authenticate
     */
    public User authenticatUser(User user) throws UserNotFoundException {
        User databaseUser = User.objects.getUser(user.getUsername());
        if (databaseUser == null) {
            throw new UserNotFoundException();
        } else {
            if (databaseUser.comparePassword(user.getRawPassword())) {
                return databaseUser;
            }
            throw new UserNotFoundException();
        }
    }
}
