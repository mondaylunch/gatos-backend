package club.mondaylunch.gatos.api.repository;

import org.springframework.stereotype.Repository;

import club.mondaylunch.gatos.api.exception.signup.EmailAlreadyInUseException;
import club.mondaylunch.gatos.api.exception.signup.UsernameAlreadyInUseException;
import club.mondaylunch.gatos.core.models.User;

@Repository
public class SignUpRepository {

    /**
     * Add a new user to the db.
     *
     * @param email             user's email
     * @param username          user's username
     * @param plaintextPassword given password
     * @throws UsernameAlreadyInUseException if the username is already in use
     * @throws EmailAlreadyInUseException    if the email is already in use
     */
    public User addUser(String email, String username, String plaintextPassword)
        throws UsernameAlreadyInUseException, EmailAlreadyInUseException {
        // Validate username and email are available
        if (this.usernameAlreadyInUse(username)) {
            throw new UsernameAlreadyInUseException();
        } else if (this.emailAlreadyInUse(email)) {
            throw new EmailAlreadyInUseException();
        }

        // Create and insert new User
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.hashPlaintextPassword(plaintextPassword);
        User.objects.insert(user);

        return user;
    }

    /**
     * Check if the username is already in use.
     *
     * @param username the username to check
     * @return true if the username is already in use
     */
    public Boolean usernameAlreadyInUse(String username) {
        return User.objects.usernameAlreadyInUse(username);
    }

    /**
     * Checks if the email is already in use.
     *
     * @param email the email to check
     * @return true if the email is already in use
     */
    public Boolean emailAlreadyInUse(String email) {
        return User.objects.emailAlreadyInUse(email);
    }
}
