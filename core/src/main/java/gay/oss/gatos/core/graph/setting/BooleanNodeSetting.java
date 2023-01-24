package gay.oss.gatos.core.graph.setting;

public class BooleanNodeSetting extends NodeSetting<Boolean> {
    public BooleanNodeSetting(Boolean value) {
        super(value);
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @Override
    public NodeSetting<Boolean> withValue(Boolean value) {
        return new BooleanNodeSetting(value);
    }
}