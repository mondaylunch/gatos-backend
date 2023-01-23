package gay.oss.gatos.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@GetMapping("/")
	public String hello(@RequestParam(value = "name", defaultValue = "Ussy...") String name) {
		return String.format("Among %s", name);
	}

	@GetMapping("/dashboard")
	public String dashboard(@RequestParam(value = "data", defaultValue = " allan please add details") String name) {
		return String.format("this is a temp page until register and login are done.", name);
	}
}