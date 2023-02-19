package club.mondaylunch.gatos.core.collection;

import static com.mongodb.client.model.Filters.eq;

import java.util.UUID;

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
        return this.getCollection().countDocuments(eq("author_id", id));
    }

}
