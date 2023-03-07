package club.mondaylunch.gatos.core;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.type.NodeType;

import java.util.ServiceLoader;

/**
 * Gatos Core entrypoint.
 */
public final class GatosCore {
    public static void init() {
        // make sure the DB is set up
        Database.checkConnection();
        // make sure datatypes are loaded & registered
        DataType.ANY.name();
        // load plugins
        ServiceLoader.load(GatosPlugin.class).forEach(GatosPlugin::init);
    }
}
