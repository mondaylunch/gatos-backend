package gay.oss.gatos.api.exceptions;

import java.util.HashMap;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmailAlreadyInUseException extends Exception {

    public static HashMap<String, String> getErrorAsJSON() {
        HashMap<String, String> error = new HashMap<>();
        error.put("error", "Email Already In Use");
        return error;
    }

}
