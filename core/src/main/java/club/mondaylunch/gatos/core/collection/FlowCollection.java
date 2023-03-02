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
        flow.getGraph()
            .observer()
            .createFlowUpdate()
            .ifPresent(updates -> this.getCollection().updateOne(Filters.eq(flow.getId()), updates));
    }
}
