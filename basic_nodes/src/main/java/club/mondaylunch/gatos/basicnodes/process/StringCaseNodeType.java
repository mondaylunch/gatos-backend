package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class StringCaseNodeType extends NodeType.Process {
    private static final DataType<CaseSetting> CASE_SETTING = DataType.register("case_setting", CaseSetting.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of("case_setting", CASE_SETTING.create(CaseSetting.UPPER));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "input", DataType.STRING));
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.STRING));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID flowId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var inputStr = DataBox.get(inputs, "input", DataType.STRING).orElseThrow();
        var setting = DataBox.get(settings, "case_setting", CASE_SETTING).orElse(CaseSetting.UPPER);
        return Map.of("output", CompletableFuture.completedFuture(DataType.STRING.create(setting.apply(inputStr))));
    }

    private enum CaseSetting {
        UPPER {
            @Override
            public <T> String apply(String str) {
                return str.toUpperCase();
            }
        },
        LOWER {
            @Override
            public <T> String apply(String str) {
                return str.toLowerCase();
            }
        };
        protected abstract <T> String apply(String str);
    }

    public static DataBox<CaseSetting> getCaseSettingOf(String setting) {
        CaseSetting op;
        try {
            op = CaseSetting.valueOf(setting.toUpperCase());
        } catch (Exception e) {
            op = CaseSetting.UPPER;
        }
        return CASE_SETTING.create(op);
    }
}
