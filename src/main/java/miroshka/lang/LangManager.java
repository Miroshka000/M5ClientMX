package miroshka.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class LangManager {

    private Map<Locale, Properties> translations;
    private Locale currentLocale;

    public LangManager() {
        translations = new HashMap<>();
        currentLocale = Locale.getDefault();
        loadTranslations();
    }

    private void loadTranslations() {
        String[] supportedLanguages = {"en", "ru"};

        for (String lang : supportedLanguages) {
            Properties properties = new Properties();
            try (InputStream input = getClass().getResourceAsStream("/miroshka/lang/" + lang + ".properties")) {
                if (input != null) {
                    InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
                    properties.load(reader);
                    translations.put(new Locale(lang), properties);
                } else {
                    System.err.println("Не удалось найти файл перевода для языка: " + lang);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке переводов для языка: " + lang);
                e.printStackTrace();
            }
        }
    }

    public void setLocale(Locale locale) {
        if (translations.containsKey(locale)) {
            currentLocale = locale;
        } else {
            System.err.println("Язык не поддерживается: " + locale);
        }
    }

    public String getTranslation(String key) {
        return translations.getOrDefault(currentLocale, new Properties()).getProperty(key, key);
    }
}
