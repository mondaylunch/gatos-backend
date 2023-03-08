package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidConnectionException extends RuntimeException {

    public InvalidConnectionException() {
        super("Invalid connection");
    }

    public InvalidConnectionException(String message) {
        super(message);
    }
}
