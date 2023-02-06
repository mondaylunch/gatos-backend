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
public class LoginControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/login";

    @BeforeEach
    public void setUp() {
        User.objects.clear();
    }

    @AfterEach
    public void tearDown() {
        this.setUp();
    }

    /// --- AUTHENTICATE ---

    @Test
    public void testLoginSuccess() throws Exception {
        this.createDefaultUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("username", DEFAULT_USERNAME),
                Map.entry("email", DEFAULT_EMAIL));
    }

    @Test
    public void testInvalidEmail() throws Exception {
        this.createDefaultUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", "invalid@example.com");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("error", "User Not Found"));
    }

    @Test
    public void testInvalidPassword() throws Exception {
        this.createDefaultUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", DEFAULT_EMAIL);
        payload.put("password", "invalid password");
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("error", "User Not Found"));
    }
}
