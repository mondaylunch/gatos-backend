package club.mondaylunch.gatos.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.repository.UserRepository;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/login")
public class LoginController {
    private final UserRepository userRepository;

    @Autowired
    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/self")
    public User fetchUser(@RequestHeader(name = "x-user-email") String userEmail) {
        return this.userRepository.getOrCreateUser(userEmail);
    }
}
