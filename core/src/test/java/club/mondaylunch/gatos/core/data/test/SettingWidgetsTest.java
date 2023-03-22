package club.mondaylunch.gatos.core.data.test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.SettingWidgets;
import club.mondaylunch.gatos.core.models.User;

public class SettingWidgetsTest {
    private final User dummyUser = new User();

    @Test
    public void canRegisterWidget() {
        class Foo {}

        var fooType = DataType.register("foo", Foo.class);
        SettingWidgets.register(fooType, SettingWidgets.Widget.TEXTAREA);
        Assertions.assertEquals(SettingWidgets.Widget.TEXTAREA, SettingWidgets.get(fooType).createWidget(this.dummyUser));
    }

    @Test
    public void canRegisterWidgetFactory() {
        class Bar {}

        var barType = DataType.register("bar", Bar.class);
        SettingWidgets.WidgetFactory widgetFactory = (user) -> SettingWidgets.Widget.CHECKBOX;
        SettingWidgets.register(barType, widgetFactory);
        Assertions.assertEquals(widgetFactory, SettingWidgets.get(barType));
    }

    @Test
    public void canGetDropdownFromEnum() {
        enum Baz {
            FOO,
            BAR,
            BAZ
        }

        var bazType = DataType.register("baz", Baz.class);
        var widget = SettingWidgets.get(bazType).createWidget(this.dummyUser);
        Assertions.assertInstanceOf(SettingWidgets.Widget.Dropdown.class, widget);
        Assertions.assertEquals(List.of("FOO", "BAR", "BAZ"), ((SettingWidgets.Widget.Dropdown) widget).getOptions());
    }
}
