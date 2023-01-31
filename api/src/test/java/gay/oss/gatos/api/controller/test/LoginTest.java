package gay.oss.gatos.api.controller.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import gay.oss.gatos.api.UserCreationHelper;
import gay.oss.gatos.api.controller.LoginController;
import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.core.models.User;

public class LoginTest implements UserCreationHelper {
    private LoginRepository loginRepo = new LoginRepository();
    private LoginController controller = new LoginController(this.loginRepo);

    @BeforeEach
    public void setUp() {
        this.clearUserDatabase();
    }

    @AfterEach
    public void resetRepo() {
        this.setUp();
    }

    @Test
    public void testFakeUserLoginFailure() {
        assert this.controller.authenticateUser(new User()).getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    public void testLoginSuccess() {
        var user = UserCreationHelper.createDefaultUser();
        var databaseUser = UserCreationHelper.createDefaultUser();
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String hash = encoder.encode(user.getPassword());
        databaseUser.setPassword(hash);
        User.objects.insert(databaseUser);
        assert this.controller.authenticateUser(user).getStatusCode() == HttpStatus.OK;
    }
}
