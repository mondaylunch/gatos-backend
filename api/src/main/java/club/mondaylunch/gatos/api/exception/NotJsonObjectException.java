package club.mondaylunch.gatos.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Body isn't a JSON object")
public class NotJsonObjectException extends RuntimeException {
}
