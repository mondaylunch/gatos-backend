package club.mondaylunch.gatos.api.controller.test;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.controller.NodeTypesController;
import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.graph.type.NodeType;

@SpringBootTest
@AutoConfigureMockMvc
public class NodeTypeControllerTest extends BaseMvcTest {

    private static final String ENDPOINT = "/api/v1/node-types";

    @BeforeAll
    public static void init() {
        BasicNodes.init();
    }

    @Test
    public void canGetNodeTypes() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var body = result.andReturn().getResponse().getContentAsString();
        var gson = new Gson();
        var type = new TypeToken<HashSet<NodeTypesController.NodeTypeInfo>>(){}.getType();
        var nodeTypes = gson.fromJson(body, type);
        var registeredNodeTypes = NodeType.REGISTRY.getEntries()
            .stream()
            .map(NodeTypesController.NodeTypeInfo::new)
            .collect(Collectors.toSet());
        Assertions.assertEquals(registeredNodeTypes, nodeTypes);
    }
}
