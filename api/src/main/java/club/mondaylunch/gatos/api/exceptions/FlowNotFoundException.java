package club.mondaylunch.gatos.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Flow Not Found")
public class FlowNotFoundException extends RuntimeException {

}