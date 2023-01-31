package gay.oss.gatos.api.controller.test;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.api.UserCreationHelper;
import gay.oss.gatos.api.controller.SignUpController;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.SignUpRepository;
import gay.oss.gatos.core.models.User;

public class SignUpTest implements UserCreationHelper {
    private SignUpRepository signUpRepo = new SignUpRepository();
    private SignUpController controller = new SignUpController(this.signUpRepo);
    private static final String KEY = "in_use";

    private UserAddedOutcomes attemptAddUser(User user) {
        try {
            this.signUpRepo.addUser(user);
        } catch (UsernameAlreadyInUseException usernameAlreadyInUseException) {
            return UserAddedOutcomes.NAME_IN_USE;
        } catch (EmailAlreadyInUseException emailAlreadyInUseException) {
            return UserAddedOutcomes.EMAIL_IN_USE;
        }
        return UserAddedOutcomes.SUCCESS;
    }

    private UserAddedOutcomes addDefaultUser() {
        return this.attemptAddUser(UserCreationHelper.createDefaultUser());
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
        assert this.addDefaultUser() == UserAddedOutcomes.SUCCESS;
    }

    @Test
    public void cannotSignUpWithUsedName() {
        this.addDefaultUser();
        assert this.attemptAddUser(UserCreationHelper.createSimpleUser(DEFAULT_USERNAME,
                "fake@example.com")) == UserAddedOutcomes.NAME_IN_USE;
    }

    @Test
    public void cannotSignUpWithUsedEmail() {
        this.addDefaultUser();
        assert this.attemptAddUser(
                UserCreationHelper.createSimpleUser("FakePerson", DEFAULT_EMAIL)) == UserAddedOutcomes.EMAIL_IN_USE;
    }

    // test for the sign-up controller
    @Test
    public void checkUsernameNotInUse() {
        var responseBody = this.controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(KEY) instanceof Boolean bool && !bool;
    }

    @Test
    public void checkUsernameInUse() {
        this.addDefaultUser();
        var responseBody = this.controller.usernameAlreadyInUse(DEFAULT_USERNAME).getBody();
        assert responseBody instanceof HashMap<?, ?> bodyMap && bodyMap.get(KEY) instanceof Boolean bool && bool;
    }

    private enum UserAddedOutcomes {
        SUCCESS,
        NAME_IN_USE,
        EMAIL_IN_USE
    }
}
