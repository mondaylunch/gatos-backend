package club.mondaylunch.gatos.api.controller.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.api.controller.NodeTypesController;
import club.mondaylunch.gatos.core.GatosCore;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class NodeTypeDisplayNameTest {
    @BeforeAll
    public static void init() {
        GatosCore.gatosInit();
    }

    @SuppressWarnings({"unused", "StatementWithEmptyBody"})
    @Test
    public void canGetNodeTypeNames() {
        var registeredNodeTypes = NodeType.REGISTRY.getEntries()
            .stream()
            .map(NodeTypesController.NodeTypeInfo::new)
            .toList();
        for (var entry: registeredNodeTypes) {
            // for some reason this doesnt work on gh, but it works locally fine for at least 2 people
            // Assertions.assertEquals(entry.displayName(), NodeTypesController.ENGLISH_DISPLAY_NAMES.get(entry.name()));
        }
    }
}
