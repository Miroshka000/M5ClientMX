package miroshka.ui.manager;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private ResourceBundle bundle;

    public LanguageManager() {
        loadLanguage("en");
    }

    public void loadLanguage(String langCode) {
        Locale locale = new Locale(langCode);
        bundle = ResourceBundle.getBundle("miroshka.ui.LanguageBundle", locale);
    }

    public String getText(String key) {
        return bundle.getString(key);
    }

    public void toggleLanguage() {
        String newLang = bundle.getLocale().getLanguage().equals("en") ? "ru" : "en";
        loadLanguage(newLang);
    }
}
