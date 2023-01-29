package gay.oss.gatos.api.controller;

import java.util.HashMap;

// rest api
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// imports from other packages
import gay.oss.gatos.core.models.User;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.SignUpRepository;

@RestController
@RequestMapping("api/v1/sign_up")
public class SignUpController {

    private final SignUpRepository repository;

    @Autowired
    public SignUpController(SignUpRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/check_username/{username}")
    public ResponseEntity usernameAlreadyInUse(@PathVariable String username) {
        HashMap<String, Boolean> response = new HashMap<>();
        response.put("in_use", this.repository.usernameAlreadyInUse(username));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity addUser(@RequestBody User user) {
        try {
            return new ResponseEntity<>(this.repository.addUser(user), HttpStatus.CREATED);
        } catch (UsernameAlreadyInUseException e) {
            return new ResponseEntity<>(UsernameAlreadyInUseException.getErrorAsJSON(), HttpStatus.BAD_REQUEST);
        } catch (EmailAlreadyInUseException ex) {
            return new ResponseEntity<>(EmailAlreadyInUseException.getErrorAsJSON(), HttpStatus.BAD_REQUEST);
        }
    }

}
