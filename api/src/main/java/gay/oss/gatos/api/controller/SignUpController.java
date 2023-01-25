package gay.oss.gatos.api.controller;

// imports from other packages
import gay.oss.gatos.core.models.User;
import gay.oss.gatos.api.repository.SignUpRepository;

// rest api stuff do not touch 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sign_up")
public class SignUpController {

    private final SignUpRepository repository;

    @Autowired
    public SignUpController(SignUpRepository repository){
        this.repository = repository;
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User addUser(@RequestBody User user) {
        return this.repository.addUser(user);
    }

}
