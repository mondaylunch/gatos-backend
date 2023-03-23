package club.mondaylunch.gatos.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.Database;
import club.mondaylunch.gatos.core.GatosCore;

@SpringBootApplication
@RestController
public class ApiApplication {
    public static void main(String[] args) {
        Database.checkConnection();
        GatosCore.gatosInit();
        SpringApplication.run(ApiApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "Gatos";
    }
}
