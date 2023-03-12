package club.mondaylunch.gatos.api;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class BaseMvcTest {
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected static final String OBJECT_EXPRESSION_PREFIX = "$.";

    @MockBean
    public JwtDecoder decoder;

    @Autowired
    protected MockMvc mockMvc;

    @SafeVarargs
    protected static ResultActions compareFields(String objectExpression, ResultActions result,
            Map.Entry<String, Object> field, Map.Entry<String, Object>... fields) {
        result = compareField(objectExpression, result, field.getKey(), field.getValue());
        for (Map.Entry<String, Object> pair : fields) {
            result = compareField(objectExpression, result, pair.getKey(), pair.getValue());
        }
        return result;
    }

    protected static ResultActions compareField(String objectExpression, ResultActions result, String fieldName,
            Object fieldValue) {
        if (fieldValue instanceof UUID) {
            fieldValue = fieldValue.toString();
        }

        try {
            return result.andExpect(MockMvcResultMatchers.jsonPath(
                    objectExpression + fieldName,
                    Matchers.is(fieldValue)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static String objectArrayExpressionPrefix(int index) {
        return "$[" + index + "].";
    }
}
