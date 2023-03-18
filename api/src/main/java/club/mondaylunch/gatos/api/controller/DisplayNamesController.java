package club.mondaylunch.gatos.api.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.GatosCore;

@RestController
@RequestMapping("api/v1/display-names")
public class DisplayNamesController {
    @GetMapping()
    public Map<String, String> getDisplayNames(@RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String acceptLanguage) {
        return GatosCore.getLang().getTranslations(acceptLanguage);
    }
}
