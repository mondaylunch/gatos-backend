package gay.oss.gatos.api.exceptions;

import java.util.HashMap;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends Exception {

    public static HashMap<String, String> getErrorAsJSON() {
        HashMap<String, String> error = new HashMap<>();
        error.put("error", "User Not Found");
        return error;
    }
}
