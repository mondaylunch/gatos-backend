package gay.oss.gatos.api.exceptions;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends Exception {

    public static HashMap<String, String> getErrorAsJSON() {
        HashMap<String, String> error = new HashMap<>();
        error.put("error", "Invalid Token");
        return error;
    }
}
