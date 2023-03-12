package club.mondaylunch.gatos.core.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BasicFlowInfo(
    @JsonProperty("_id") UUID id,
    String name,
    String description,
    @JsonProperty("author_id") UUID authorId
) {
    public BasicFlowInfo(Flow flow) {
        this(flow.getId(), flow.getName(), flow.getDescription(), flow.getAuthorId());
    }
}
