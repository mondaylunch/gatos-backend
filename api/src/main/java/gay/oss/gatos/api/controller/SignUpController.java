package gay.oss.gatos.api.controller;

// imports from other packages
import gay.oss.gatos.core.models.User;
import gay.oss.gatos.api.repository.SignUpRepository;

// rest api stuff do not touch 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign_up")
public class SignUpController {

    private final SignUpRepository repository;

    @Autowired
    public SignUpController(SignUpRepository repository){
        this.repository = repository;
    }
    
    @PostMapping
    public User addUser(@PathVariable User user) {
        return this.repository.addUser(user);
    }

}
