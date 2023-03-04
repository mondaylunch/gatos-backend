package club.mondaylunch.gatos.api;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.Database;

@SpringBootApplication
@RestController
public class ApiApplication {

    public static void main(String[] args) {
        Database.checkConnection();
        SpringApplication.run(ApiApplication.class, args);
        BasicNodes.init();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:5173");
            }
        };
    }

    @GetMapping("/")
    public String hello(@RequestParam(value = "name", defaultValue = "Us") String name) {
        return String.format("Among %s", name);
    }
}
