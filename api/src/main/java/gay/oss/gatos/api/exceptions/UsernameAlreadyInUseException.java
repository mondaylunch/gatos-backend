package gay.oss.gatos.api.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyInUseException extends Exception {
    
}
