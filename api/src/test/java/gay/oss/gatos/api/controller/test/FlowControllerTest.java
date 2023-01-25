package gay.oss.gatos.api.controller.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import gay.oss.gatos.api.controller.FlowController;
import gay.oss.gatos.core.models.Flow;

@WebMvcTest(FlowController.class)
public class FlowControllerTest {

    private static final UUID ZERO_UUID = new UUID(0, 0);

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.reset();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        Flow.objects.clear();
    }

    @Test
    public void canGetNoFlows() throws Exception {
        this.testGetFlows(0);
    }

    @Test
    public void canGetOneFlow() throws Exception {
        this.testGetFlows(1);
    }

    @Test
    public void canGetManyFlows() throws Exception {
        this.testGetFlows(10);
    }

    private void testGetFlows(int flowCount) throws Exception {
        List<Flow> flows = new ArrayList<>();
        for (int i = 0; i < flowCount; i++) {
            Flow flow = new Flow("Test Flow " + i, ZERO_UUID);
            Flow.objects.insert(flow);
            flows.add(flow);
        }
        ResultActions result = this.getFlows(flowCount);
        for (int i = 0; i < flowCount; i++) {
            result = this.compareFlow(flows.get(i), i, result);
        }
    }

    private ResultActions getFlows(int expectedFlowCount) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/flows/list")
                .header("x-auth-token", "")
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(expectedFlowCount)));
    }

    private ResultActions compareFlow(Flow flow, int index, ResultActions result) throws Exception {
        return this.compareFields(index, result,
            Map.entry("name", flow.getName()),
            Map.entry("authorId", flow.getAuthorId())
        );
    }

    @SafeVarargs
    private ResultActions compareFields(int index, ResultActions result, Map.Entry<String, Object> field, Map.Entry<String, Object>... fields) throws Exception {
        result = result.andExpect(MockMvcResultMatchers.jsonPath(
            "$[" + index + "]." + field.getKey(),
            Matchers.is(field.getValue().toString())
        ));
        for (Map.Entry<String, Object> pair : fields) {
            result = result.andExpect(MockMvcResultMatchers.jsonPath(
                "$[" + index + "]." + pair.getKey(),
                Matchers.is(pair.getValue().toString())
            ));
        }
        return result;
    }
}
