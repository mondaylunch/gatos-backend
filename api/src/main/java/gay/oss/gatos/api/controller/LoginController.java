package gay.oss.gatos.api.controller;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.api.repository.LoginRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/login")
public class LoginController {

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
    public ResponseEntity authenticateUser(@Valid @RequestBody BodyAuthenticate data) {
        try {
            return new ResponseEntity<>(this.repository.authenticateUser(data.email, data.password), HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(UserNotFoundException.getErrorAsJSON(), HttpStatus.NOT_FOUND);
        }
    }

}
