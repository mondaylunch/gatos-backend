package club.mondaylunch.gatos.core.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import club.mondaylunch.gatos.core.models.User;

/**
 * Associates datatypes with widgets for node settings.
 */
public final class SettingWidgets {
    private static final Map<DataType<?>, WidgetFactory> WIDGET_FACTORIES = new HashMap<>();

    /**
     * Associates a datatype with a widget.
     * @param type              the datatype
     * @param widget            the widget
     */
    public static <T> void register(DataType<T> type, Widget widget) {
        WIDGET_FACTORIES.put(type, $ -> widget);
    }

    /**
     * Associates a datatype with a widget factory.
     * @param type              the datatype
     * @param widgetFactory     the widget factory
     */
    public static <T> void register(DataType<T> type, WidgetFactory widgetFactory) {
        WIDGET_FACTORIES.put(type, widgetFactory);
    }

    /**
     * Gets the widget factory for a given datatype.
     * @param type  the datatype
     * @return      the widget factory
     */
    public static WidgetFactory get(DataType<?> type) {
        var factory = WIDGET_FACTORIES.get(type);
        if (factory == null) {
            if (type.clazz().isEnum()) {
                return Widget.dropdown(enumValueFunc(type));
            } else {
                return $ -> Widget.TEXTBOX;
            }
        }

        return factory;
    }

    private static Function<User, List<String>> enumValueFunc(DataType<?> type) {
        return u -> Arrays.stream(type.clazz().getEnumConstants()).map(e -> ((Enum<?>) e).name()).toList();
    }

    /**
     * A function that returns valid dropdown options for a given datatype.
     */
    @FunctionalInterface
    public interface WidgetFactory {
        Widget createWidget(User user);
    }

    /**
     * Represents a widget for a node setting.
     */
    public static sealed class Widget {
        public static final Widget TEXTBOX = new Widget("textbox");
        public static final Widget TEXTAREA = new Widget("textarea");
        public static final Widget CHECKBOX = new Widget("checkbox");
        public static final Widget NUMBERBOX = new Widget("numberbox");

        private final String name;

        private Widget(String name) {
            this.name = name;
        }

        /**
         * Creates a dropdown widget.
         * @param func  a function to create the list of options for a given user
         * @return      the dropdown widget
         */
        public static WidgetFactory dropdown(Function<User, List<String>> func) {
            return user -> new Dropdown(func.apply(user));
        }

        /**
         * Gets the name of the widget.
         * @return  the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Represents a dropdown widget.
         */
        public static final class Dropdown extends Widget {
            private final List<String> options;

            private Dropdown(List<String> options) {
                super("dropdown");
                this.options = options;
            }

            /**
             * Gets the list of options for the dropdown.
             * @return  the options
             */
            public List<String> getOptions() {
                return this.options;
            }
        }
    }
}
