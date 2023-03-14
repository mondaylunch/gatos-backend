package club.mondaylunch.gatos.api.controller;

import java.io.FileReader;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.mondaylunch.gatos.core.graph.type.NodeType;

@RestController
@RequestMapping("api/v1/node-types")
public class NodeTypesController {
    public static final Map<String, String> ENGLISH_DISPLAY_NAMES = getTranslatedDisplayNameMap("en_gb");

    public record NodeTypeInfo(String name, String category) {
        public NodeTypeInfo(Map.Entry<String, NodeType> entry) {
            this(entry.getKey(), getDisplayName(entry.getValue().category().toString().toLowerCase()));
        }

        // currently only english is implemented. can be expanded if language selection is added.
        private static String getDisplayName(String registryName) {
            var displayName = ENGLISH_DISPLAY_NAMES.get(registryName);
            return displayName == null ? registryName : displayName;
        }
    }

    @GetMapping
    public List<NodeTypeInfo> getNodeTypes() {
        return NodeType.REGISTRY.getEntries()
            .stream()
            .map(NodeTypeInfo::new)
            .toList();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getTranslatedDisplayNameMap(String langFile) {
        Gson gson = new Gson();
        var file = ClassLoader.getSystemClassLoader().getResource("display_names/" + langFile + ".json");
        try {
            return gson.fromJson(new FileReader(file == null ? "" : file.getFile()), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
