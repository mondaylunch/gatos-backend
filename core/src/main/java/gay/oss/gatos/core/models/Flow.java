package gay.oss.gatos.core.models;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonProperty;

import gay.oss.gatos.core.collections.FlowCollection;

/**
 * POJO for flows.
 */
public class Flow extends BaseModel {

    public static FlowCollection objects = new FlowCollection();

    /**
     * Display name.
     */
    private String name;

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
