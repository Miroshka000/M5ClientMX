package miroshka.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE_PATH;

    static {
        File jarFile = null;
        try {
            jarFile = new File(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String executableDir = jarFile.getParent();
        CONFIG_FILE_PATH = executableDir + File.separator + "miroshka_config.properties";
    }

    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            loadConfig();
        } else {
            setDefaultConfig();
            saveConfig();
        }
    }

    public void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (configFile.exists()) {
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException e) {
        }
    }

    private void setDefaultConfig() {
        properties.setProperty("language", "ru");
        properties.setProperty("device", "Plus2");
        properties.setProperty("comPort", "COM1");
        properties.setProperty("devMode", "false");
        properties.setProperty("autoUpdate", "false");
        properties.setProperty("notifications", "false");
    }

    public void saveConfig() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                properties.store(writer, "Конфигурация");
            }
        } catch (IOException e) {
        }
    }

    public String getConfigValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setConfigValue(String key, String value) {
        properties.setProperty(key, value);
    }

    public void setLanguage(String language) {
        setConfigValue("language", language);
        saveConfig();
    }
}
