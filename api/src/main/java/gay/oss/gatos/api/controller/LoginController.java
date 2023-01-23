package gay.oss.gatos.api.controller;

// imports from other packages
import gay.oss.gatos.api.model.UserModel;
import gay.oss.gatos.api.repository.LoginRepository;
import gay.oss.gatos.api.exceptions.UserNotFoundException;

// rest api stuff do not touch 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginRepository repository;

    @Autowired
    public LoginController(LoginRepository repository){
        this.repository = repository;
    }

    @GetMapping("/username/{username}")
    public String findByUsername(@PathVariable String username) throws UserNotFoundException {
        return this.repository.findByUsername(username);
    }

}
