package club.mondaylunch.gatos.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.Flow;

/**
 * Gatos Core entrypoint.
 */
public final class GatosCore implements GatosPlugin {
    private static final List<GatosPlugin> PLUGINS = new ArrayList<>();
    private static GatosLang LANG;

    /**
     * Gets a list of all loaded plugins.
     * @return  the loaded plugins
     */
    public static List<GatosPlugin> getPlugins() {
        return List.copyOf(PLUGINS);
    }

    /**
     * Gets the language handler.
     * @return  the language handler
     */
    public static GatosLang getLang() {
        return LANG;
    }

    public static void gatosInit() {
        // make sure the DB is set up
        Database.checkConnection();
        // make sure datatypes are loaded & registered
        DataType.ANY.name();
        // load plugins
        ServiceLoader.load(GatosPlugin.class).forEach(plugin -> {
            plugin.init();
            PLUGINS.add(plugin);
        });
        // load language files
        LANG = new GatosLang();
        // setup flow triggers
        setupAllFlowTriggers();
    }

    @Override
    public void init() {
    }

    @Override
    public String name() {
        return "core";
    }

    private static void setupAllFlowTriggers() {
        Flow.objects.getCollection().find().forEach(Flow::setupTriggers);
    }
}
