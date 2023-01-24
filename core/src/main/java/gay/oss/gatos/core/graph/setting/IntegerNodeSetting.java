package gay.oss.gatos.core.graph.setting;

public class IntegerNodeSetting extends NodeSetting<Integer> {
    public IntegerNodeSetting(Integer value) {
        super(value);
    }

    @Override
    public Class<Integer> getTypeClass() {
        return Integer.class;
    }

    @Override
    public String getTypeName() {
        return "integer";
    }

    @Override
    public NodeSetting<Integer> withValue(Integer value) {
        return new IntegerNodeSetting(value);
    }
}