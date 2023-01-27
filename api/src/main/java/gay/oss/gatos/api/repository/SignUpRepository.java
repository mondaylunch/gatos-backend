package gay.oss.gatos.api.repository;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.core.models.User;

@Repository
public class SignUpRepository {

    /**
     * add a new user to the db.
     *
     * @param User user
     * @throws UsernameAlreadyInUseException
     * @throws EmailAlreadyInUseException
     */
    public User addUser(User user) throws UsernameAlreadyInUseException, EmailAlreadyInUseException {
        // validate if we can add the user
        if (this.usernameAlreadyInUse(user.getUsername())) {
            throw new UsernameAlreadyInUseException();
        } else if (this.emailAlreadyInUse(user.getEmail())) {
            throw new EmailAlreadyInUseException();
        } else {
            Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
            String hash = encoder.encode(user.getPassword());
            user.setPassword(hash);
            User.objects.insert(user);
        }
        return user;
    }

    /**
     * check if the username is already in use.
     *
     * @param String username
     * @return true if the username is already in use
     */
    public Boolean usernameAlreadyInUse(String username) {
        return User.objects.usernameAlreadyInUse(username);
    }

    /**
     * checks if the email is already in use.
     *
     * @param String email
     * @return true if the email is already in use
     */
    public Boolean emailAlreadyInUse(String email) {
        return User.objects.emailAlreadyInUse(email);
    }

}
