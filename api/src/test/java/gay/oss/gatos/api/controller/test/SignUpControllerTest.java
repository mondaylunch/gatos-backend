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
import gay.oss.gatos.api.exceptions.EmailAlreadyInUseException;
import gay.oss.gatos.api.exceptions.UsernameAlreadyInUseException;
import gay.oss.gatos.api.helpers.UserCreationHelper;
import gay.oss.gatos.core.models.User;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SignUpControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/sign_up";

    @BeforeAll
    static void setUp() {
        User.objects.clear();
    }

    @AfterAll
    static void cleanUp() {
        User.objects.clear();
    }

    /// --- ADD USER ---

    @Test
    public void testSignUpSuccess() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "signup@example.com");
        payload.put("username", "signup");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("username", "signup"),
                Map.entry("email", "signup@example.com"));
    }

    @Test
    public void testSignUpConflictingEmail() throws Exception {
        this.createSimpleUser("uniqueusername", "conflicting@example.com");

        Map<String, String> payload = new HashMap<>();
        payload.put("email", "conflicting@example.com");
        payload.put("username", "uniqueusername1");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof EmailAlreadyInUseException));
    }

    @Test
    public void testSignUpConflictingUsername() throws Exception {
        this.createSimpleUser("conflicting_username", "unique@email.com");

        Map<String, String> payload = new HashMap<>();
        payload.put("email", "unique1@email.com");
        payload.put("username", "conflicting_username");
        payload.put("password", DEFAULT_PASSWORD);
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(res -> assertTrue(res.getResolvedException() instanceof UsernameAlreadyInUseException));
    }

    @Test
    public void testSignUpInvalidUsername() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "valid_email_1@example.com");
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
        payload.put("email", "valid_email_2@example.com");
        payload.put("username", "validusername2");
        payload.put("password", "a");
        String userJson = MAPPER.writeValueAsString(payload);

        this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /// --- CHECK USERNAME ---

    @Test
    public void testCheckUsernameIsInUse() throws Exception {
        var user = this.createRandomUser();

        ResultActions result = this.mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT + "/check_username/" + user.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("in_use", true));
    }

    @Test
    public void testCheckUsernameIsNotInUse() throws Exception {
        ResultActions result = this.mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT + "/check_username/" + "unused_username"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("in_use", false));
    }
}
