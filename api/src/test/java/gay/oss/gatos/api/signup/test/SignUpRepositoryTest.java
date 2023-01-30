package gay.oss.gatos.api.signup.test;
import gay.oss.gatos.api.controller.SignUpController;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.SignUpRepository;
import gay.oss.gatos.core.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SignUpRepositoryTest {
    private SignUpRepository signUpRepo = new SignUpRepository();
    private static final String DEFAULT_USERNAME = "RealPerson";
    private static final String DEFAULT_EMAIL = "jeroenisthebest@example.com";

    private UserAddedOutcomes attemptAddUser(User user) {
        try {
            signUpRepo.addUser(user);
        } catch (UsernameAlreadyInUseException usernameAlreadyInUseException) {
            return UserAddedOutcomes.NAME_IN_USE;
        } catch (EmailAlreadyInUseException emailAlreadyInUseException) {
            return UserAddedOutcomes.EMAIL_IN_USE;
        }
        return UserAddedOutcomes.SUCCESS;
    }

    private User createSimpleUser(String username, String email) {
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("Kolling22");
        return user;
    }

    private UserAddedOutcomes addDefaultUser() {
        return attemptAddUser(createSimpleUser(DEFAULT_USERNAME, DEFAULT_EMAIL));
    }

    @BeforeEach
    public void setUp() {
        this.signUpRepo = new SignUpRepository();
        User.objects.clear();
    }

    @AfterEach
    public void resetRepo() {
        this.setUp();
    }

    @Test
    public void canSignUp() {
        assert addDefaultUser() == UserAddedOutcomes.SUCCESS;
    }

    @Test
    public void cannotSignUpWithUsedName() {
        addDefaultUser();
        assert attemptAddUser(createSimpleUser(DEFAULT_USERNAME, "fake@example.com")) == UserAddedOutcomes.NAME_IN_USE;
    }

    @Test
    public void cannotSignUpWithUsedEmail() {
        addDefaultUser();
        assert attemptAddUser(createSimpleUser("FakePerson", DEFAULT_EMAIL)) == UserAddedOutcomes.EMAIL_IN_USE;
    }

    // test for the sign-up controller
    @Test
    public void checkUsernameNotInUse() {
        var key = "in_use";
        var controller = new SignUpController(this.signUpRepo);
        var responseBody = controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(key) instanceof Boolean bool && !bool;
    }

    @Test
    public void checkUsernameInUse() {
        var key = "in_use";
        var controller = new SignUpController(this.signUpRepo);
        addDefaultUser();
        var responseBody = controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(key) instanceof Boolean bool && bool;
    }

    private enum UserAddedOutcomes {
        SUCCESS,
        NAME_IN_USE,
        EMAIL_IN_USE
    }
}
