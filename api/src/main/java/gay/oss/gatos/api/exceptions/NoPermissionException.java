package gay.oss.gatos.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "No Permission")
public class NoPermissionException extends RuntimeException {

}
