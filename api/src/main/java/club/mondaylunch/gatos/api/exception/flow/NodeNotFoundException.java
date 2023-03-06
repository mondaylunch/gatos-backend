package club.mondaylunch.gatos.api.exception.flow;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(UUID nodeId) {
        super("Node not found: " + nodeId);
    }
}
