package club.mondaylunch.gatos.api;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.Database;
import club.mondaylunch.gatos.core.GatosCore;

@SpringBootApplication
@RestController
public class ApiApplication {
    public static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);

    public static void main(String[] args) {
        Database.checkConnection();
        GatosCore.gatosInit();
        SpringApplication.run(ApiApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "Gatos";
    }

    @GetMapping("/whopper")
    public String whopper() {
        return EASTER_EGGS[new Random().nextInt(EASTER_EGGS.length)];
    }

    private static final String[] EASTER_EGGS = new String[] {
        "<iframe width=\"1366\" height=\"651\" src=\"https://www.youtube.com/embed/QH2-TGUlwu4\" title=\"Nyan Cat [original]\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>",
        "<iframe width=\"1366\" height=\"482\" src=\"https://www.youtube.com/embed/9cPxh2DikIA\" title=\"Whopper Whopper (Extended)\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>",
        "<iframe width=\"956\" height=\"538\" src=\"https://www.youtube.com/embed/lW6ekG-FFiM\" title=\"Cats headbutt vine boom\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>",
        "<html><head></head><body><style>@keyframes wobble {0% { transform: scale(0.5); }50% { transform: scale(1.2); }100% { transform: scale(0.5); }}</style><h1 style=\"text-align:center;\">JEREON KEPPENS</h1><img src=\"https://nms.kcl.ac.uk/jeroen.keppens/images/jeroen.jpg\" style=\"margin-left:auto;margin-right:auto;animation-name: wobble;animation-play-state: running;animation-duration: 0.5s;animation-iteration-count: infinite;margin-left: auto;margin-right: auto;\"></body></html>"
    };
}
