package gay.oss.gatos.core.graph;

public record NodeMetadata(float xPos, float yPos) {
    public NodeMetadata withX(float value) {
        return new NodeMetadata(value, this.yPos);
    }

    public NodeMetadata withY(float value) {
        return new NodeMetadata(this.xPos, value);
    }
}
