package gay.oss.gatos.api.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.exceptions.FlowNotFoundException;
import gay.oss.gatos.api.exceptions.NoPermissionException;
import gay.oss.gatos.core.models.Flow;
import gay.oss.gatos.core.models.User;

@Repository
public class FlowRepository {
    /**
     * Get a flow by ID for a given User.
     * @param user   User to check permissions against
     * @param flowId Flow to fetch from database
     */
    public Flow getFlow(User user, UUID flowId) throws FlowNotFoundException, NoPermissionException {
        Flow flow = Flow.objects.get(flowId);
        if (flow == null) {
            throw new FlowNotFoundException();
        }

        if (!user.getId().equals(flow.getAuthorId())) {
            throw new NoPermissionException();
        }

        return flow;
    }
}
