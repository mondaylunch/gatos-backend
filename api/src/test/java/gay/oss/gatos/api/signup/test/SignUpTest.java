package gay.oss.gatos.api.signup.test;
import gay.oss.gatos.api.UserCreationHelper;
import gay.oss.gatos.api.controller.LoginController;
import gay.oss.gatos.api.controller.SignUpController;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.api.repository.SignUpRepository;
import gay.oss.gatos.core.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SignUpTest implements UserCreationHelper {
    private SignUpRepository signUpRepo = new SignUpRepository();
    private SignUpController controller = new SignUpController(this.signUpRepo);
    private static final String KEY = "in_use";

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

    private UserAddedOutcomes addDefaultUser() {
        return attemptAddUser(UserCreationHelper.createDefaultUser());
    }

    @BeforeEach
    public void setUp() {
        this.clearUserDatabase();
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
        //var xy = new LoginController(new LoginRepository()).authenticateUser(UserCreationHelper.createDefaultUser());
        assert attemptAddUser(UserCreationHelper.createSimpleUser(DEFAULT_USERNAME, "fake@example.com")) == UserAddedOutcomes.NAME_IN_USE;
    }

    @Test
    public void cannotSignUpWithUsedEmail() {
        addDefaultUser();
        assert attemptAddUser(UserCreationHelper.createSimpleUser("FakePerson", DEFAULT_EMAIL)) == UserAddedOutcomes.EMAIL_IN_USE;
    }

    // test for the sign-up controller
    @Test
    public void checkUsernameNotInUse() {
        var responseBody = controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(KEY) instanceof Boolean bool && !bool;
    }

    @Test
    public void checkUsernameInUse() {
        addDefaultUser();
        var responseBody = controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(KEY) instanceof Boolean bool && bool;
    }

    private enum UserAddedOutcomes {
        SUCCESS,
        NAME_IN_USE,
        EMAIL_IN_USE
    }
}
