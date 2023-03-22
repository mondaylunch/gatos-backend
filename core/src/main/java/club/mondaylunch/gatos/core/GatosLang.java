package club.mondaylunch.gatos.core;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Handles localised display names.
 */
public class GatosLang {
    /**
     * The locales supported. Translation files are only loaded if their locale is in this list.
     */
    private static final List<Locale> SUPPORTED_LOCALES = List.of(
        new Locale.Builder().setLanguage("en").build()
    );

    private final Map<Locale, Map<String, String>> translations;

    public GatosLang() {
        Map<Locale, Map<String, String>> translations = new HashMap<>();
        for (var locale : SUPPORTED_LOCALES) {
            Map<String, String> localeTranslations = new HashMap<>();
            GatosPlugin.getResources("lang/" + locale.toLanguageTag() + ".json")
                .map(GatosLang::loadLangFile)
                .forEach(localeTranslations::putAll);
            translations.put(locale, Map.copyOf(localeTranslations));
        }
        this.translations = Map.copyOf(translations);
    }

    /**
     * Gets the translations for the given language range. This will fill in gaps in higher-priority languages
     * with translations from lower-priority languages. Even if the language range does not mention the default
     * language (the first element of {@link #SUPPORTED_LOCALES}), it will be used as a fallback.
     * @param languageRange the language range to use
     * @return              the translations
     */
    public Map<String, String> getTranslations(String languageRange) {
        Map<String, String> result = new HashMap<>();
        var locales = Locale.filter(Locale.LanguageRange.parse(languageRange), SUPPORTED_LOCALES);
        locales.add(SUPPORTED_LOCALES.get(0));
        for (var locale : locales) {
            var localeTranslations = this.translations.get(locale);
            if (localeTranslations == null) {
                continue;
            }

            for (var entry : localeTranslations.entrySet()) {
                result.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private static Map<String, String> loadLangFile(String fileContents) {
        return new Gson().fromJson(fileContents, new TypeToken<HashMap<String, String>>(){}.getType());
    }
}
