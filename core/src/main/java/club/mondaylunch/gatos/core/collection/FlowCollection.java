package gay.oss.gatos.core.collection;

import gay.oss.gatos.core.models.Flow;

/**
 * "flows" collection for {@link Flow}.
 */
public class FlowCollection extends BaseCollection<Flow> {

    public FlowCollection() {
        super("flows", Flow.class);
    }
}
