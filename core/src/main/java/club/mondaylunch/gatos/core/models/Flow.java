package club.mondaylunch.gatos.core.models;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonProperty;

import club.mondaylunch.gatos.core.collection.FlowCollection;

/**
 * POJO for flows.
 */
public class Flow extends BaseModel {

    public static final FlowCollection objects = new FlowCollection();

    public Flow(String name, UUID authorId) {
        this.name = name;
        this.authorId = authorId;
    }

    public Flow() {
    }

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
    private UUID authorId;

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
}
