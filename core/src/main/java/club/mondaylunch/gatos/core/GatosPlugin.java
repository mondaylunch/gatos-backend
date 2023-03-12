package club.mondaylunch.gatos.core;

/**
 * Entrypoint for Gatos plugins. Plugins are loaded via the Java ServiceLoader, so they must be
 * declared in a META-INF/services/club.mondaylunch.gatos.core.GatosPlugin file.
 */
public interface GatosPlugin {
    /**
     * Initialise this plugin. Make sure all your plugin's registrable things are registered.
     */
    void init();
}
