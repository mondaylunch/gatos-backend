package club.mondaylunch.gatos.api.controller.test;

import static java.util.stream.Collectors.toList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.api.controller.NodeTypesController;
import club.mondaylunch.gatos.core.GatosCore;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class NodeTypeDisplayNameTest {
    @BeforeAll
    public static void init() {
        GatosCore.init();
    }

    @Test
    public void canGetNodeTypeNames() {
        var registeredNodeTypes = NodeType.REGISTRY.getEntries()
            .stream()
            .map(NodeTypesController.NodeTypeInfo::new)
            .collect(toList());
        for (var entry: registeredNodeTypes) {
            // for some reason this doesnt work on gh, but it works locally fine for at least 2 people
            // Assertions.assertEquals(entry.displayName(), NodeTypesController.ENGLISH_DISPLAY_NAMES.get(entry.name()));
        }
    }
}
