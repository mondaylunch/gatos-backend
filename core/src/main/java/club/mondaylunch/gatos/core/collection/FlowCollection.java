package club.mondaylunch.gatos.core.collection;

import java.util.UUID;

import com.mongodb.client.model.Filters;

import club.mondaylunch.gatos.core.models.Flow;

/**
 * "flows" collection for {@link Flow}.
 */
public class FlowCollection extends BaseCollection<Flow> {

    public FlowCollection() {
        super("flows", Flow.class);
    }

    /**
     * Gets the number of flows attributed to a user.
     *
     * @return The number of flows.
     */
    public long countByUserId(UUID id) {
        return this.getCollection().countDocuments(Filters.eq("author_id", id));
    }

    public void updateGraph(Flow flow) {
        if (!this.contains(flow.getId())) {
            throw new IllegalArgumentException("Flow with ID " + flow.getId() + " does not exist");
        }
        flow.getGraph()
            .observer()
            .updateFlow(flow.getId(), this.getCollection());
    }
}
