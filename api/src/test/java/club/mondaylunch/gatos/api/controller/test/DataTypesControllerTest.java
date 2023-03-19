package club.mondaylunch.gatos.api.controller.test;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.HashSet;
import java.util.Set;

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
import club.mondaylunch.gatos.api.controller.DataTypesController;
import club.mondaylunch.gatos.core.GatosCore;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;

@SpringBootTest
@AutoConfigureMockMvc
public class DataTypesControllerTest extends BaseMvcTest {

    private static final String ENDPOINT = "/api/v1/data-types";

    @BeforeAll
    public static void init() {
        GatosCore.init();
    }

    @BeforeEach
    public void setupMockJwt() {
        Mockito.when(this.decoder.decode(anyString())).thenReturn(TestSecurity.jwt());
    }

    @Test
    public void canGetConversions() throws Exception {
        var type1 = DataType.register("foo", Class1.class);
        var type2 = DataType.register("bar", Class2.class);
        var type3 = DataType.register("baz", Class3.class);
        Conversions.register(type1, type2, $ -> new Class2());
        Conversions.register(type2, type3, $ -> new Class3());
        var result = this.mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/conversions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestSecurity.FAKE_TOKEN))
            .andExpect(MockMvcResultMatchers.status().isOk());
        var body = result.andReturn().getResponse().getContentAsString();
        var gson = new Gson();
        var type = new TypeToken<HashSet<DataTypesController.ConversionInfo>>() {
        }.getType();
        Set<DataTypesController.ConversionInfo> conversions = gson.fromJson(body, type);
        assertContainsConversion(conversions, type1, type2);
        assertContainsConversion(conversions, type2, type3);
        assertContainsConversion(conversions, type1, type3);
    }

    private static void assertContainsConversion(Set<DataTypesController.ConversionInfo> conversions, DataType<?> typeA, DataType<?> typeB) {
        Assertions.assertTrue(conversions.contains(new DataTypesController.ConversionInfo(DataType.REGISTRY.getName(typeA).orElseThrow(), DataType.REGISTRY.getName(typeB).orElseThrow())));
    }

    private record Class1() {
    }

    private record Class2() {
    }

    private record Class3() {
    }
}
