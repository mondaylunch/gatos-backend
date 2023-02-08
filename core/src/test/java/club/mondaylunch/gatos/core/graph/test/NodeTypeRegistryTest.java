package club.mondaylunch.gatos.core.graph.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.type.NodeCategory;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.graph.type.NodeTypeRegistry;

public class NodeTypeRegistryTest {
    @Test
    public void canAddAndRetrieve() {
        var node = new NodeType() {
            @Override
            public NodeCategory category() {
                return NodeCategory.PROCESS;
            }

            @Override
            public Map<String, DataBox<?>> settings() {
                return Map.of();
            }
        };

        Assertions.assertEquals(node, NodeTypeRegistry.register("my_node", node));
        var res = NodeTypeRegistry.get("my_node");
        Assertions.assertTrue(res.isPresent());
        Assertions.assertEquals(node, res.get());
    }

    @Test
    public void retrievingNonexistentGivesEmptyOptional() {
        Assertions.assertFalse(NodeTypeRegistry.get("nonexistent_node").isPresent());
    }
}
