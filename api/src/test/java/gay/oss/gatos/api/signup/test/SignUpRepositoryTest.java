package gay.oss.gatos.api.signup.test;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.SignUpRepository;
import gay.oss.gatos.core.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void setUp() {
        this.signUpRepo = new SignUpRepository();
        User.objects.clear();
    }

    @AfterEach
    public void resetRepo() {
        this.setUp();
    }

    private UserAddedOutcomes addDefaultUser() {
        return attemptAddUser(createSimpleUser(DEFAULT_USERNAME, DEFAULT_EMAIL));
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

    private enum UserAddedOutcomes {
        SUCCESS,
        NAME_IN_USE,
        EMAIL_IN_USE
    }
}
