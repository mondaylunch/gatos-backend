package gay.oss.gatos.api.controller.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import gay.oss.gatos.api.BaseMvcTest;
import gay.oss.gatos.api.exceptions.UserNotFoundException;
import gay.oss.gatos.api.helpers.UserCreationHelper;
import gay.oss.gatos.core.models.User;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class LoginControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/login";

    @BeforeAll
    static void setUp() {
        User.objects.clear();
    }

    @AfterAll
    static void cleanUp() {
        User.objects.clear();
    }

    /// --- AUTHENTICATE ---

    @Test
    public void testLoginSuccess() throws Exception {
        var user = this.createRandomUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("username", user.getUsername()),
                Map.entry("email", user.getEmail()));
    }

    @Test
    public void testInvalidEmail() throws Exception {
        this.createRandomUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", "invalid@example.com");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof UserNotFoundException));
    }

    @Test
    public void testInvalidPassword() throws Exception {
        var user = this.createRandomUser();

        Map<String, String> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        payload.put("password", "invalid password");
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof UserNotFoundException));
    }
}
