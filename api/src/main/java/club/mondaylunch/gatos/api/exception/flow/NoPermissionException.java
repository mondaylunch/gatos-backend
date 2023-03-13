package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "No Permission")
public class NoPermissionException extends RuntimeException {

}
