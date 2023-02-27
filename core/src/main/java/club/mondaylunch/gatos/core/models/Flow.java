package club.mondaylunch.gatos.core.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.collection.FlowCollection;
import club.mondaylunch.gatos.core.graph.Graph;

/**
 * POJO for flows.
 */
public class Flow extends BaseModel {

    public static final FlowCollection objects = new FlowCollection();

    /**
     * Display name.
     */
    private String name;

    /**
     * Display description.
     */
    private String description = "";

    /**
     * UUID of the user who owns this flow.
     */
    @BsonProperty("author_id")
    @JsonProperty("author_id")
    private UUID authorId;

    private Graph graph = new Graph();

    public Flow(UUID id, String name, UUID authorId) {
        super(id);
        this.name = name;
        this.authorId = authorId;
    }

    public Flow() {
    }

    /**
     * Get the display name.
     *
     * @return display name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the display name.
     *
     * @param name display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the description.
     *
     * @return description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the author's UUID.
     *
     * @return author's UUID
     */
    public UUID getAuthorId() {
        return this.authorId;
    }

    /**
     * Set the author's UUID.
     *
     * @param authorId author's UUID
     */
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    /**
     * Get the graph.
     *
     * @return the graph.
     */
    public Graph getGraph() {
        return this.graph;
    }

    /**
     * Set the graph.
     *
     * @param graph the graph.
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
