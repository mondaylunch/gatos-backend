package club.mondaylunch.gatos.api.controller;

import java.util.HashMap;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.api.repository.SignUpRepository;
import club.mondaylunch.gatos.core.models.User;

@RestController
@RequestMapping("api/v1/sign_up")
public class SignUpController {

    private final SignUpRepository repository;

    @Autowired
    public SignUpController(SignUpRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/check_username/{username}")
    public ResponseEntity<HashMap<String, Boolean>> usernameAlreadyInUse(@PathVariable String username) {
        HashMap<String, Boolean> response = new HashMap<>();
        response.put("in_use", this.repository.usernameAlreadyInUse(username));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private record BodyAddUser(
            @NotNull @Email String email,
            @NotNull @Length(min = 2, max = 32) String username,
            @NotNull @Length(min = 8) String password) {
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody BodyAddUser data) {
        return new ResponseEntity<>(this.repository.addUser(data.email, data.username, data.password),
                HttpStatus.CREATED);
    }

}
