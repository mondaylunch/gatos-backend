package gay.oss.gatos.core.graph;

/**
 * Metadata for a node, needed for displaying in the editor.
 * @param xPos  the X-Coordinate
 * @param yPos  the Y-Coordinate
 */
public record NodeMetadata(float xPos, float yPos) {
    /**
     * Create a new metadata with the given X-Coordinate value
     * @param value the X-Coordinate value
     * @return  a new metadata, the same as this but with the given X-Coordinate
     */
    public NodeMetadata withX(float value) {
        return new NodeMetadata(value, this.yPos);
    }
    /**
     * Create a new metadata with the given Y-Coordinate value
     * @param value the Y-Coordinate value
     * @return  a new metadata, the same as this but with the given Y-Coordinate
     */
    public NodeMetadata withY(float value) {
        return new NodeMetadata(this.xPos, value);
    }
}
