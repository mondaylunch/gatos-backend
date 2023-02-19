package club.mondaylunch.gatos.api.controller;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import club.mondaylunch.gatos.api.repository.LoginRepository;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/login")
public class LoginController {
    private final Random random = new SecureRandom();
    private final LoginRepository repository;

    @Autowired
    public LoginController(LoginRepository repository) {
        this.repository = repository;
    }

    private record BodyAuthenticate(
        @NotNull @Email String email,
        @NotNull @Length(min = 8) String password) {
    }

    @PostMapping("/authenticate")
    public User authenticateUser(@Valid @RequestBody BodyAuthenticate data) {
        User user = this.repository.authenticateUser(data.email, data.password);

        // Generate random authentication token
        byte[] salt = new byte[64];
        this.random.nextBytes(salt);
        user.setAuthToken(new String(Base64.getEncoder().encode(salt)));
        User.objects.update(user.getId(), user);

        return user;
    }

    @GetMapping("/self")
    public User fetchUser(@RequestHeader("x-auth-token") String token) {
        return this.repository.authenticateUser(token);
    }

}
