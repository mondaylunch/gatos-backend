package club.mondaylunch.gatos.api.controller.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.jayway.jsonpath.JsonPath;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.helpers.UserCreationHelper;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

@SpringBootTest
@AutoConfigureMockMvc
public class FlowControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/flows";
    private User user;

    @BeforeEach
    void setUp() {
        this.user = this.createRandomUser();
    }

    @AfterAll
    static void cleanUp() {
        Flow.objects.clear();
    }

    /// --- LIST FLOWS ---

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
    public void cannotGetFlowsWithInvalidToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list")
                .header("x-auth-token", "invalid"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    /// --- ADD FLOW ---

    @Test
    public void canAddFlow() throws Exception {
        Flow flow = new Flow();
        flow.setName("My Flow");
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(1);
        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("name", flow.getName()),
                Map.entry("authorId", this.user.getId()));
    }

    @Test
    public void cannotAddFlowWithoutBody() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithInvalidBody() throws Exception {
        String invalidJson = "invalid";

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithoutToken() throws Exception {
        Flow flow = createFlow(new User());
        String flowJson = MAPPER.writeValueAsString(flow);
        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.assertFlowCount(0);
    }

    @Test
    public void cannotAddFlowWithoutContentType() throws Exception {
        Flow flow = createFlow(this.user);
        String flowJson = MAPPER.writeValueAsString(flow);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .header("x-auth-token", this.user.getAuthToken())
                .content(flowJson));
        Assertions.assertThrows(
                AssertionError.class,
                () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(0);
    }

    /// --- UPDATE FLOW ---

    @Test
    public void cannotUpdateNoFlowFields() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void canUpdateFlowName() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(1);
        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("name", update.getName()),
                Map.entry("authorId", flow.getAuthorId()));
        Flow newFlow = getFlow(result);
        Assertions.assertNotNull(newFlow);
        Assertions.assertEquals(update.getName(), newFlow.getName());
        Assertions.assertEquals(flow.getAuthorId(), newFlow.getAuthorId());
    }

    @Test
    public void cannotUpdateFlowAuthorId() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setAuthorId(UUID.randomUUID());
        String flowJson = MAPPER.writeValueAsString(update);
        this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutToken() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson));
        Assertions.assertThrows(
                AssertionError.class,
                () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    @Test
    public void cannotUpdateFlowWithoutContentType() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        Flow update = new Flow();
        update.setName("New Name");
        String flowJson = MAPPER.writeValueAsString(update);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken())
                .content(flowJson));
        Assertions.assertThrows(
                AssertionError.class,
                () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Flow newFlow = Flow.objects.get(flow.getId());
        assertFlowEquality(flow, newFlow);
    }

    /// --- DELETE FLOW ---

    @Test
    public void canDeleteFlow() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId())
                .header("x-auth-token", this.user.getAuthToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());
        this.assertFlowCount(0);
        Assertions.assertNull(Flow.objects.get(flow.getId()));
    }

    @Test
    public void cannotDeleteNonExistentFlow() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + UUID.randomUUID())
                .header("x-auth-token", this.user.getAuthToken()));
        Assertions.assertThrows(
                AssertionError.class,
                () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(0);
    }

    @Test
    public void cannotDeleteFlowWithoutToken() throws Exception {
        Flow flow = createFlow(this.user);
        Flow.objects.insert(flow);
        this.assertFlowCount(1);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "/" + flow.getId()));
        Assertions.assertThrows(
                AssertionError.class,
                () -> result.andExpect(MockMvcResultMatchers.status().isOk()));
        this.assertFlowCount(1);
        Assertions.assertNotNull(Flow.objects.get(flow.getId()));
    }

    /// --- UTILITIES ---

    private void assertFlowCount(long count) {
        Assertions.assertEquals(count, this.getFlowCount());
    }

    private long getFlowCount() {
        return Flow.objects.countByUserId(this.user.getId());
    }

    private static Flow createFlow(User author) {
        return new Flow("Test Flow", author.getId());
    }

    private static Flow getFlow(ResultActions result) throws Exception {
        String responseJson = result.andReturn().getResponse().getContentAsString();
        UUID flowId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        return Flow.objects.get(flowId);
    }

    private void testGetFlows(int flowCount) throws Exception {
        List<Flow> flows = new ArrayList<>();
        for (int i = 0; i < flowCount; i++) {
            Flow flow = new Flow("Test Flow " + i, this.user.getId());
            Flow.objects.insert(flow);
            flows.add(flow);
        }
        ResultActions result = this.getFlows(this.user, flowCount);
        for (int i = 0; i < flowCount; i++) {
            result = compareFlow(flows.get(i), i, result);
        }
    }

    private ResultActions getFlows(User user, int expectedFlowCount) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/list")
                .header("x-auth-token", this.user.getAuthToken()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$",
                        Matchers.hasSize(expectedFlowCount)));
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
                Map.entry("authorId", flow.getAuthorId()));
    }
}
