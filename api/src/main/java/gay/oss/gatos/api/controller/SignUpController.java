package gay.oss.gatos.api.controller;

// imports from other packages
import gay.oss.gatos.core.models.User;
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.repository.SignUpRepository;

// rest api stuff do not touch 
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/sign_up")
public class SignUpController {

    private final SignUpRepository repository;

    @Autowired
    public SignUpController(SignUpRepository repository){
        this.repository = repository;
    }

    @GetMapping("/check_username/{username}")
    public Boolean usernameAlreadyInUse(@PathVariable String username) {
        return this.repository.usernameAlreadyInUse(username);
    }

    @GetMapping("/check_email/{email}")
    public Boolean emailAlreadyInUse(@PathVariable String email) {
        return this.repository.emailAlreadyInUse(email);
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User addUser(@RequestBody User user) throws UsernameAlreadyInUseException, EmailAlreadyInUseException {
        return this.repository.addUser(user);
    }

}
