package club.mondaylunch.gatos.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.auth.GatosOidcUser;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/login")
public class LoginController {
    @GetMapping("/self")
    public User fetchUser(@AuthenticationPrincipal GatosOidcUser principal) {
        return principal.getUser();
    }
}
