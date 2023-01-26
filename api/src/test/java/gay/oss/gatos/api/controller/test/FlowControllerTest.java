package gay.oss.gatos.api.controller.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import gay.oss.gatos.api.controller.FlowController;
import gay.oss.gatos.core.models.Flow;

@WebMvcTest(FlowController.class)
public class FlowControllerTest {

    private static final String ENDPOINT = "/api/v1/flows";
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String OBJECT_EXPRESSION_PREFIX = "$.";

    @Autowired
    private MockMvc mockMvc;
    private long initialFlowCount;

    @BeforeEach
    void setUp() {
        this.reset();
        this.initialFlowCount = getFlowCount();
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

    @Test
    public void cannotGetFlowsWithoutToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void canAddFlow() throws Exception {
        Flow flow = createFlow();
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCountChange(1);
        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
            Map.entry("name", flow.getName()),
            Map.entry("authorId", flow.getAuthorId())
        );
        Flow newFlow = getFlow(result);
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotAddFlowWithoutBody() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", "")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCountChange(0);
    }

    @Test
    public void cannotAddFlowWithInvalidBody() throws Exception {
        String invalidJson = "invalid";
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .header("x-auth-token", "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson)
        );
        this.assertFlowCountChange(0);
    }

    @Test
    public void cannotAddFlowWithoutToken() throws Exception {
        Flow flow = createFlow();
        String flowJson = MAPPER.writeValueAsString(flow);
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCountChange(0);
    }

    @Test
    public void cannotAddFlowWithoutContentType() throws Exception {
        Flow flow = createFlow();
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .header("x-auth-token", "")
            .content(flowJson)
        );
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk())
        );
        this.assertFlowCountChange(0);
    }

    @Test
    public void cannotUpdateNoFlowFields() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow update = new Flow();
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header("x-auth-token", "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson)
        );
        this.assertFlowCountChange(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void canUpdateFlowName() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson)
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCountChange(1);
        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
            Map.entry("name", update.getName()),
            Map.entry("authorId", flow.getAuthorId())
        );
        Flow newFlow = getFlow(result);
        Assertions.assertNotNull(newFlow);
        Assertions.assertEquals(update.getName(), newFlow.getName());
        Assertions.assertEquals(flow.getAuthorId(), newFlow.getAuthorId());
    }

    @Test
    public void cannotUpdateFlowAuthorId() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setAuthorId(UUID.randomUUID());
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header("x-auth-token", "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson)
        );
        this.assertFlowCountChange(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutToken() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(flowJson)
        );
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk())
        );
        this.assertFlowCountChange(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutContentType() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
            .header("x-auth-token", "")
            .content(flowJson)
        );
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk())
        );
        this.assertFlowCountChange(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void canDeleteFlow() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", "")
            )
            .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCountChange(0);
        Assertions.assertNull(Flow.objects.get(flow.getId()));
    }

    @Test
    public void cannotDeleteNonExistentFlow() throws Exception {
        Flow flow = createFlow();
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId())
            .header("x-auth-token", "")
        );
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk())
        );
        this.assertFlowCountChange(0);
    }

    @Test
    public void cannotDeleteFlowWithoutToken() throws Exception {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId()));
        Assertions.assertThrows(
            AssertionError.class,
            () -> result.andExpect(MockMvcResultMatchers.status().isOk())
        );
        this.assertFlowCountChange(1);
        Assertions.assertNotNull(Flow.objects.get(flow.getId()));
    }

    private void assertFlowCountChange(long change) {
        Assertions.assertEquals(this.initialFlowCount + change, getFlowCount());
    }

    private static long getFlowCount() {
        return Flow.objects.size();
    }

    private static Flow createFlow() {
        return new Flow("Test Flow", ZERO_UUID);
    }

    private static Flow getFlow(ResultActions result) throws Exception {
        String responseJson = result.andReturn().getResponse().getContentAsString();
        UUID flowId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        return Flow.objects.get(flowId);
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
            result = compareFlow(flows.get(i), i, result);
        }
    }

    private ResultActions getFlows(int expectedFlowCount) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list")
                .header("x-auth-token", "")
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(expectedFlowCount)));
    }

    private static void assertFlowEquality(Flow oldFlow, Flow newFlow) {
        Assertions.assertNotNull(oldFlow);
        Assertions.assertNotNull(newFlow);
        Assertions.assertEquals(oldFlow.getName(), newFlow.getName());
        Assertions.assertEquals(oldFlow.getAuthorId(), newFlow.getAuthorId());
    }

    private static ResultActions compareFlow(Flow flow, int index, ResultActions result) throws Exception {
        return compareFields(objectArrayExpressionPrefix(index), result,
            Map.entry("name", flow.getName()),
            Map.entry("authorId", flow.getAuthorId())
        );
    }

    @SafeVarargs
    private static ResultActions compareFields(String objectExpression, ResultActions result, Map.Entry<String, Object> field, Map.Entry<String, Object>... fields) throws Exception {
        result = compareField(objectExpression, result, field.getKey(), field.getValue());
        for (Map.Entry<String, Object> pair : fields) {
            result = compareField(objectExpression, result, pair.getKey(), pair.getValue());
        }
        return result;
    }

    private static ResultActions compareField(String objectExpression, ResultActions result, String fieldName, Object fieldValue) throws Exception {
        return result.andExpect(MockMvcResultMatchers.jsonPath(
            objectExpression + fieldName,
            Matchers.is(fieldValue.toString())
        ));
    }

    private static String objectArrayExpressionPrefix(int index) {
        return "$[" + index + "].";
    }
}
