package club.mondaylunch.gatos.api.exception.flow;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error executing flow")
public class FlowExecutionException extends RuntimeException {

    public FlowExecutionException(Throwable cause) {
        super(cause);
    }
}
