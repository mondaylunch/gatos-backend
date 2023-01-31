package gay.oss.gatos.api.repository;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
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
    public User authenticateUser(User user) throws UserNotFoundException {
        User databaseUser = User.objects.getUserUsingEmail(user.getEmail());
        if (databaseUser == null) {
            throw new UserNotFoundException();
        } else {
            Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
            var math = encoder.matches(user.getPassword(), databaseUser.getPassword());
            if (encoder.matches(user.getPassword(), databaseUser.getPassword())) {
                return databaseUser;
            }
            throw new UserNotFoundException();
        }
    }
}
