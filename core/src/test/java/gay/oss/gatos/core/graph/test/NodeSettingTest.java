package gay.oss.gatos.core.graph.test;

import gay.oss.gatos.core.graph.setting.IntegerNodeSetting;
import gay.oss.gatos.core.graph.setting.NodeSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NodeSettingTest {
    @Test
    public void canCreateSetting() {
        var setting = new IntegerNodeSetting(5);
        Assertions.assertEquals(5, setting.value());
    }

    @Test
    public void canMakeNewSettingWithValue() {
        var setting = new IntegerNodeSetting(5);
        var newSetting = setting.withValue(10);
        Assertions.assertEquals(10, newSetting.value());
        Assertions.assertEquals(5, setting.value());
    }
}
