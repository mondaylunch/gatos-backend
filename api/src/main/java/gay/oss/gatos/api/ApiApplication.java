package gay.oss.gatos.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gay.oss.gatos.core.Database;

@SpringBootApplication
@RestController
public class ApiApplication {

    public static void main(String[] args) {
        Database.checkConnection();
        SpringApplication.run(ApiApplication.class, args);
    }

    @GetMapping("/")
    public String hello(@RequestParam(value = "name", defaultValue = "Us") String name) {
        return String.format("Among %s", name);
    }
}
