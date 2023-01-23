package gay.oss.gatos.core.graph;

public record NodeSetting<T>(
        NodeSettingType<T> settingType,
        String name,
        String description,
        T value) {
}
