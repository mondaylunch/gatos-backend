package club.mondaylunch.gatos.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.graph.type.NodeType;

@RestController
@RequestMapping("api/v1/node-types")
public class NodeTypesController {

    public record NodeTypeInfo(String name, String category) {
        public NodeTypeInfo(Map.Entry<String, NodeType> entry) {
            this(entry.getKey(), entry.getValue().category().toString().toLowerCase());
        }
    }

    @GetMapping
    public List<NodeTypeInfo> getNodeTypes() {
        return NodeType.REGISTRY.getEntries()
            .stream()
            .map(NodeTypeInfo::new)
            .toList();
    }
}
