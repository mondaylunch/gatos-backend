package club.mondaylunch.gatos.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import club.mondaylunch.gatos.api.auth.GatosOidcUser;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/login")
public class LoginController {
    @GetMapping("/self")
    public User fetchUser(@AuthenticationPrincipal GatosOidcUser principal) {
        return principal.getUser();
    }

    @GetMapping("/is-logged-in")
    public boolean isLoggedIn(@AuthenticationPrincipal GatosOidcUser principal) {
        return principal != null;
    }

    @GetMapping("/login")
    public RedirectView login(@AuthenticationPrincipal GatosOidcUser principal, @RequestParam(value = "redirect", defaultValue = "/") String redirect) {
        // if we're not logged in, spring will have already redirected us,
        // so we can just return a redirect to the requested page here
        return new RedirectView(redirect);
    }
}
