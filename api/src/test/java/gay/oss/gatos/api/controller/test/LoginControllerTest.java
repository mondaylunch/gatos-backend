package gay.oss.gatos.api.controller.test;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import gay.oss.gatos.api.controller.LoginController;
import gay.oss.gatos.core.models.User;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {
    private static final String ENDPOINT = "/api/login";
    private static final UUID ZERO_UUID = new UUID(0, 0);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    void setUp() {
        this.reset();   // reset the db first.
        this.setUser(); // setup the user
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    @Test
    public void testUserCanLoginWithRightCredentials() throws Exception {
        User.objects.insert(this.user);
        String userJSON = MAPPER.writeValueAsString(this.user);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJSON))
                                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUserCanNotLoginWithWrongEmail() throws Exception {
        User.objects.insert(this.user);
        this.user.setEmail("wrong.email@example.org");
        String userJSON = MAPPER.writeValueAsString(this.user);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJSON))
                                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testUserCanNotLoginWithWrongPassword() throws Exception {
        String wrongPassword = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("wrongPassword123");
        this.user.setPassword(wrongPassword);
        User.objects.insert(this.user);
        String userJSON = MAPPER.writeValueAsString(this.user);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJSON))
                                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private void reset() {
        User.objects.clear();
    }

    private void setUser() {
        this.user.setId(ZERO_UUID);
        this.user.setEmail("test.user@email.com");
        this.user.setUsername("@test_user");
        String password = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("Password123");
        this.user.setPassword(password);
    }

}
