package club.mondaylunch.gatos.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Email Already In Use")
public class EmailAlreadyInUseException extends RuntimeException {

}
