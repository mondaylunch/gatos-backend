package club.mondaylunch.gatos.api.exception.signup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Username Already In Use")
public class UsernameAlreadyInUseException extends RuntimeException {

}
