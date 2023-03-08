package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Invalid data type")
public class InvalidDataTypeException extends RuntimeException {
}
