package club.mondaylunch.gatos.api.controller.test;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
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
            .collect(Collectors.toList());
        for (var entry: registeredNodeTypes) {
            Assertions.assertEquals(entry.displayName(), NodeTypesController.ENGLISH_DISPLAY_NAMES.get(entry.name()));
        }
    }
}
