package gay.oss.gatos.core.graph;

public record NodeSetting<T>(
        Class<T> settingType,
        String name,
        String description,
        T value) {

    public NodeSetting<T> withValue(T value) {
        return new NodeSetting<>(this.settingType, this.name, this.description, value);
    }
}
