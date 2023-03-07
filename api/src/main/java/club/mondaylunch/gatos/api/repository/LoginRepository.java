package club.mondaylunch.gatos.api.repository;

import org.springframework.stereotype.Repository;

import club.mondaylunch.gatos.api.exception.login.InvalidTokenException;
import club.mondaylunch.gatos.api.exception.login.UserNotFoundException;
import club.mondaylunch.gatos.core.models.User;

@Repository
public class LoginRepository {
    /**
     * Authenticate and fetch user by email and password.
     *
     * @param email    user's email
     * @param password given password
     */
    public User authenticateUser(String email, String password) throws UserNotFoundException {
        User user = User.objects.getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        } else {
            if (user.validatePassword(password)) {
                return user;
            }
            throw new UserNotFoundException();
        }
    }

    /**
     * Authenticate and fetch user by auth token.
     *
     * @param authToken user's auth token
     */
    public User authenticateUser(String authToken) throws InvalidTokenException {
        User user = User.objects.getUserByToken(authToken);
        if (user == null) {
            throw new InvalidTokenException();
        }

        return user;
    }
}
