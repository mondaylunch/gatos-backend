package gay.oss.gatos.api;

import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class BaseMvcTest {
    @Autowired
    protected MockMvc mockMvc;

    @SafeVarargs
    protected static ResultActions compareFields(String objectExpression, ResultActions result,
            Map.Entry<String, Object> field, Map.Entry<String, Object>... fields) throws Exception {
        result = compareField(objectExpression, result, field.getKey(), field.getValue());
        for (Map.Entry<String, Object> pair : fields) {
            result = compareField(objectExpression, result, pair.getKey(), pair.getValue());
        }
        return result;
    }

    protected static ResultActions compareField(String objectExpression, ResultActions result, String fieldName,
            Object fieldValue) throws Exception {
        return result.andExpect(MockMvcResultMatchers.jsonPath(
                objectExpression + fieldName,
                Matchers.is(fieldValue.toString())));
    }

    protected static String objectArrayExpressionPrefix(int index) {
        return "$[" + index + "].";
    }
}
