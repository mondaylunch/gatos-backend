package club.mondaylunch.gatos.core;

import com.mongodb.assertions.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GatosLangTest {
    @BeforeAll
    public static void init() {
        GatosCore.gatosInit();
    }

    @Test
    public void canGetTranslations() {
        var translations = GatosCore.getLang().getTranslations("en");
        Assertions.assertTrue(translations.get("test_translation").equals("Test Translation!"));
    }

    @Test
    public void fallsBackWhenAskedForSpecificLocale() {
        var translations = GatosCore.getLang().getTranslations("zh,en-US,en-GB");
        Assertions.assertTrue(translations.get("test_translation").equals("Test Translation!"));
    }
}
