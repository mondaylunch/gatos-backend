package club.mondaylunch.gatos.api.controller.test;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.TestSecurity;
import club.mondaylunch.gatos.api.helpers.UserCreationHelper;
import club.mondaylunch.gatos.core.models.User;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class LoginControllerTest extends BaseMvcTest implements UserCreationHelper {
    private static final String ENDPOINT = "/api/v1/login";
    private static final String USER_EMAIL = "dentarthur.dent@42.com";
    private static final String USER_DISCORD_ID = "42".repeat(10);

    @BeforeAll
    static void setUp() {
        User.objects.clear();
        var arthur = new User();
        arthur.setEmail(USER_EMAIL);
        arthur.setDiscordId(USER_DISCORD_ID);
        User.objects.insert(arthur);
    }

    @BeforeEach
    public void setupMockJwt() {
        Mockito.when(this.decoder.decode(anyString())).thenReturn(TestSecurity.jwt());
    }

    @AfterAll
    static void cleanUp() {
        User.objects.clear();
    }

    /// --- SELF ---

    @Test
    public void testGetSelfWithExistingUser() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/self")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", USER_EMAIL))
                .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("email", USER_EMAIL),
                Map.entry("discord_id", USER_DISCORD_ID));
    }

    @Test
    public void testGetSelfWithNewUser() throws Exception {
        String otherUserEmail = "prefect@ford.com";
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/self")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header("x-user-email", otherUserEmail))
            .andExpect(MockMvcResultMatchers.status().isOk());

        result = compareFields(OBJECT_EXPRESSION_PREFIX, result,
                Map.entry("email", otherUserEmail));
    }
}
