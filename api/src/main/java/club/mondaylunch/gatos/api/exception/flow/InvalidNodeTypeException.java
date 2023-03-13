package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class InvalidNodeTypeException extends RuntimeException {

    public InvalidNodeTypeException() {
        super("Invalid node type");
    }

    public InvalidNodeTypeException(String message) {
        super(message);
    }
}
