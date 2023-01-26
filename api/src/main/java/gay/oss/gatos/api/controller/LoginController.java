package gay.oss.gatos.api.controller;

// rest api
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
// imports from other packages
import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.core.models.User;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginRepository repository;

    @Autowired
    public LoginController(LoginRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity authenticatUser(@RequestBody User user) {
        try {
            return new ResponseEntity<>(this.repository.authenticatUser(user), HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(UserNotFoundException.getErrorAsJSON(), HttpStatus.NOT_FOUND);
        }
    }

}
