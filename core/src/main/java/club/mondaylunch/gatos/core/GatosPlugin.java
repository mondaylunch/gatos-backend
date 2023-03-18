package club.mondaylunch.gatos.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * Entrypoint for Gatos plugins. Plugins are loaded via the Java ServiceLoader, so they must be
 * declared in a META-INF/services/club.mondaylunch.gatos.core.GatosPlugin file.
 */
public interface GatosPlugin {
    /**
     * Initialise this plugin. Make sure all your plugin's registrable things are registered.
     */
    void init();

    /**
     * Unique name. Used in paths to discriminate between files provided by different plugins.
     * @return the name of this plugin
     */
    String name();

    /**
     * Get the contents of a text resource provided by a plugin.
     * @param plugin    the plugin to get the resource from
     * @param path      the (unqualified) path to the resource
     * @return          the contents of the resource, or null if the resource does not exist
     */
    static @Nullable String getResource(GatosPlugin plugin, String path) {
        String qualifiedPath = plugin.name() + "/" + path;
        var stream = plugin.getClass().getResourceAsStream(qualifiedPath);
        if (stream == null) {
            return null;
        }
        try (stream) {
            return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            //TODO log this
            return null;
        }
    }

    /**
     * Get a stream of the contents of all instances of a text resource.
     * @param path  the (unqualified) path to the resource
     * @return      a stream of the contents of all instances of the resource
     */
    static Stream<String> getResources(String path) {
        return GatosCore.getPlugins().stream()
            .map(plugin -> getResource(plugin, path))
            .filter(Objects::nonNull);
    }
}
