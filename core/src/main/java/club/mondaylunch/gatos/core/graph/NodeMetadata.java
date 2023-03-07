package club.mondaylunch.gatos.core.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * Metadata for a node, needed for displaying in the editor.
 * @param xPos the X-Coordinate
 * @param yPos the Y-Coordinate
 */
public record NodeMetadata(
    @BsonProperty("x_pos")
    @JsonProperty("x_pos")
    float xPos,
    @BsonProperty("y_pos")
    @JsonProperty("y_pos")
    float yPos
) {
    /**
     * Create a new metadata with the given X-Coordinate value.
     * @param value the X-Coordinate value
     * @return a new metadata, the same as this but with the given X-Coordinate
     */
    public NodeMetadata withX(float value) {
        return new NodeMetadata(value, this.yPos);
    }

    /**
     * Create a new metadata with the given Y-Coordinate value.
     * @param value the Y-Coordinate value
     * @return a new metadata, the same as this but with the given Y-Coordinate
     */
    public NodeMetadata withY(float value) {
        return new NodeMetadata(this.xPos, value);
    }
}
