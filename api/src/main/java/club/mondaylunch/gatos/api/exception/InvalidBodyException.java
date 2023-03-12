package club.mondaylunch.gatos.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidBodyException extends RuntimeException {

    public InvalidBodyException() {
        super("Invalid body");
    }

    public InvalidBodyException(String message) {
        super(message);
    }
}
