package gay.oss.gatos.api.controller;

// imports from other packages
import gay.oss.gatos.core.models.User;
import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.api.exceptions.UserNotFoundException;

// rest api stuff do not touch 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginRepository repository;

    @Autowired
    public LoginController(LoginRepository repository){
        this.repository = repository;
    }
    
    @GetMapping("/get/{username}/{password}")
    public User getUser(@PathVariable("username") String username, @PathVariable("password") String password) throws UserNotFoundException {
        return this.repository.validateUser(username, password);
    }

}
