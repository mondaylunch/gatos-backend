package gay.oss.gatos.api.controller;

// rest api
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// imports from other packages
import gay.oss.gatos.api.repository.LoginRepository;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginRepository repository;

    @Autowired
    public LoginController(LoginRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/get/{username}/{password}")
    public ResponseEntity getUser(@PathVariable("username") String username, @PathVariable("password") String password) {
        try {
            return new ResponseEntity<>(this.repository.validateUser(username, password), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
        }
    }

}
