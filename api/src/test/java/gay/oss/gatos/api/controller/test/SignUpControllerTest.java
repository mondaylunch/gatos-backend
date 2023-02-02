package gay.oss.gatos.api.controller.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import gay.oss.gatos.api.BaseMvcTest;
import gay.oss.gatos.api.helpers.UserCreationHelper;
import gay.oss.gatos.core.models.User;

@SpringBootTest
@AutoConfigureMockMvc
public class SignUpControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/sign_up";

    @BeforeEach
    public void setUp() {
        User.objects.clear();
    }

    @AfterEach
    public void tearDown() {
        this.setUp();
    }

    /// --- ADD USER ---

    @Test
    public void testSignUpSuccess() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("username", DEFAULT_USERNAME);
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("username", DEFAULT_USERNAME),
                Map.entry("email", DEFAULT_EMAIL));
    }

    @Test
    public void testSignUpConflictingEmail() throws Exception {
        this.createSimpleUser("uniqueusername", DEFAULT_EMAIL);

        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("username", DEFAULT_USERNAME);
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("error", "Email Already In Use"));
    }

    @Test
    public void testSignUpConflictingUsername() throws Exception {
        this.createSimpleUser(DEFAULT_USERNAME, "unique@email.com");

        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("username", DEFAULT_USERNAME);
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("error", "Username Already In Use"));
    }

    @Test
    public void testSignUpInvalidUsername() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("username", "a");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSignUpInvalidPassword() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("username", DEFAULT_USERNAME);
        payload.put("password", "a");
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
