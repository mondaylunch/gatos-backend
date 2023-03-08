package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidNodeSettingException extends RuntimeException {

    public InvalidNodeSettingException(String message) {
        super(message);
    }
}
