package club.mondaylunch.gatos.api.controller.test;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import club.mondaylunch.gatos.api.BaseMvcTest;
import club.mondaylunch.gatos.api.TestSecurity;
import club.mondaylunch.gatos.core.GatosCore;

@SpringBootTest
@AutoConfigureMockMvc
public class DisplayNamesControllerTest extends BaseMvcTest {

    private static final String ENDPOINT = "/api/v1/display-names";

    @BeforeAll
    public static void init() {
        GatosCore.gatosInit();
    }

    @BeforeEach
    public void setupMockJwt() {
        Mockito.when(this.decoder.decode(anyString())).thenReturn(TestSecurity.jwt());
    }

    @Test
    public void canGetDisplayNames() throws Exception {
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var body = result.andReturn().getResponse().getContentAsString();
        var gson = new Gson();
        var type = new TypeToken<HashMap<String, String>>(){}.getType();
        var names = gson.fromJson(body, type);
        var actualNames = GatosCore.getLang().getTranslations("en");
        Assertions.assertEquals(actualNames, names);
    }
}
