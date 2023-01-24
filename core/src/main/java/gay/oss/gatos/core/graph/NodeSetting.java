package gay.oss.gatos.core.graph;

/**
 * A setting on a node
 * @param settingType   the class of data this setting holds
 * @param name          the name of this setting
 * @param description   the description for this setting
 * @param value         the data this setting holds
 * @param <T>           the type of data this setting holds
 */
public record NodeSetting<T>(
        Class<T> settingType,
        String name,
        String description,
        T value) {

    public NodeSetting<T> withValue(T value) {
        return new NodeSetting<>(this.settingType, this.name, this.description, value);
    }
}
