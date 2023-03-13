package club.mondaylunch.gatos.core.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import club.mondaylunch.gatos.core.Database;
import club.mondaylunch.gatos.core.models.BasicFlowInfo;
import club.mondaylunch.gatos.core.models.Flow;

/**
 * "flows" collection for {@link Flow}.
 */
public class FlowCollection extends BaseCollection<Flow> {

    private final MongoCollection<Document> documentCollection;

    public FlowCollection() {
        super("flows", Flow.class);
        this.documentCollection = Database.getCollection("flows");
    }

    public BasicFlowInfo getBasicInfo(UUID id) {
        var document = this.documentCollection
            .find(Filters.eq(id))
            .limit(1)
            .first();
        if (document == null) {
            return null;
        } else {
            return create(document);
        }
    }

    public List<BasicFlowInfo> getBasicInfo(String field, Object value) {
        List<BasicFlowInfo> flows = new ArrayList<>();
        for (var document : this.documentCollection.find(Filters.eq(field, value))) {
            flows.add(create(document));
        }
        return flows;
    }

    private static BasicFlowInfo create(Document document) {
        return new BasicFlowInfo(
            document.get("_id", UUID.class),
            document.get("name", String.class),
            document.get("description", String.class),
            document.get("author_id", UUID.class)
        );
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
