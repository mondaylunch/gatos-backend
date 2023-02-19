package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.core.models.User;

@Repository
public class LoginRepository {
    /**
     * Authenticate and fetch user by email and password.
     *
     * @param email    user's email
     * @param password given password
     */
    public User authenticateUser(String email, String password) throws UserNotFoundException {
        User user = User.objects.getUserUsingEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        } else {
            if (user.validatePassword(password)) {
                return user;
            }
            throw new UserNotFoundException();
        }
    }
}
