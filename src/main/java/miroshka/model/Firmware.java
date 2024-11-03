package miroshka.model;

import javafx.application.Platform;
import miroshka.view.CustomAlert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;

public enum Firmware {
    CATHACK("CatHack", "/miroshka/images/cathack.png", "#ff8e19", "DYNAMIC", "DYNAMIC", "DYNAMIC"),
    BRUCE("Bruce", "/miroshka/images/bruce.png", "#a82da4", "DYNAMIC", "DYNAMIC", "DYNAMIC"),
    NEMO("Nemo", "/miroshka/images/nemo.png", "#7d9f71",
            "https://github.com/n0xa/m5stick-nemo/releases/download/v2.7.0/M5Nemo-v2.7.0-M5StickCPlus2.bin",
            "https://github.com/n0xa/m5stick-nemo/releases/download/v2.7.0/M5Nemo-v2.7.0-M5Cardputer.bin",
            "https://github.com/n0xa/m5stick-nemo/releases/download/v2.7.0/M5Nemo-v2.7.0-M5StickCPlus.bin"),
    MARAUDER("Marauder", "/miroshka/images/marauder.png", "#c3c3c3",
            "https://m5burner-cdn.m5stack.com/firmware/b732d70a74405f7f1c6e961fa4d17f37.bin",
            "https://m5burner-cdn.m5stack.com/firmware/3397b17ad7fd314603abf40954a65369.bin",
            "https://m5burner-cdn.m5stack.com/firmware/aeb96d4fec972a53f934f8da62ab7341.bin"),
    M5LAUNCHER("M5Launcher", "/miroshka/images/m5launcher.png", "#9cb597",
            "https://github.com/bmorcelli/M5Stick-Launcher/releases/latest/download/Launcher-m5stack-cplus2.bin",
            "https://github.com/bmorcelli/M5Stick-Launcher/releases/latest/download/Launcher-m5stack-cardputer.bin",
            "https://github.com/bmorcelli/M5Stick-Launcher/releases/latest/download/Launcher-m5stack-cplus1_1.bin"),
    USERDEMO("UserDemo", "/miroshka/images/userdemo.png", "#fab320",
            "https://github.com/m5stack/M5StickCPlus2-UserDemo/releases/download/V0.1/K016-P2-M5StickCPlus2-UserDemo-V0.1_0x0.bin",
            "https://github.com/m5stack/M5Cardputer-UserDemo/releases/download/V0.9/K132-Cardputer-UserDemo-V0.9_0x0.bin",
            "UNAVAILABLE");

    private final String displayName;
    private final String imagePath;
    private final String buttonColor;
    private final String plus2Url;
    private final String cardUrl;
    private final String plus1Url;

    Firmware(String displayName, String imagePath, String buttonColor, String plus2Url, String cardUrl, String plus1Url) {
        this.displayName = displayName;
        this.imagePath = imagePath;
        this.buttonColor = buttonColor;
        this.plus2Url = plus2Url;
        this.cardUrl = cardUrl;
        this.plus1Url = plus1Url;
    }
    public String getImagePath() {
        return imagePath;
    }

    public String getButtonColor() {
        return buttonColor;
    }

    public String getDownloadUrl(String device) {
        if (this == USERDEMO && device.equals("Plus1")) {
            throw new IllegalArgumentException("UserDemo firmware is not available for Plus1.");
        }
        switch (device) {
            case "Plus2":
                return resolveUrl(plus2Url, device);
            case "Card":
                return resolveUrl(cardUrl, device);
            case "Plus1":
                return resolveUrl(plus1Url, device);
            default:
                throw new IllegalArgumentException("Unknown device: " + device);
        }
    }

    private String resolveUrl(String url, String device) {
        if ("DYNAMIC".equals(url)) {
            return fetchLatestReleaseUrl(device);
        }
        if ("UNAVAILABLE".equals(url)) {
            throw new IllegalArgumentException("Firmware is not available for the selected device.");
        }
        return url;
    }

    private String fetchLatestReleaseUrl(String device) {
        try {
            String apiUrl = this == BRUCE
                    ? "https://api.github.com/repos/pr3y/Bruce/releases/latest"
                    : "https://api.github.com/repos/Stachugit/CatHack/releases/latest";
            HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                Platform.runLater(() -> {
                    try {
                        new CustomAlert("Error", "Failed to fetch latest release: HTTP error code " + conn.getResponseCode(), CustomAlert.AlertType.ERROR).showAndWait();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            String jsonResponse = scanner.useDelimiter("\\A").next();
            scanner.close();
            conn.disconnect();

            JSONObject releaseData = new JSONObject(jsonResponse);
            JSONArray assets = releaseData.getJSONArray("assets");

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String name = asset.getString("name").toLowerCase();
                if ((device.equals("Plus2") && name.contains("plus2")) ||
                        (device.equals("Plus1") && name.contains("plus") && !name.contains("plus2")) ||
                        (device.equals("Card") && name.contains("cardputer"))) {
                    return asset.getString("browser_download_url");
                }
            }
            throw new RuntimeException("The firmware for the device was not found.");
        } catch (IOException e) {
            Platform.runLater(() -> new CustomAlert("Error", "Failed to fetch the latest firmware URL: " + e.getMessage(), CustomAlert.AlertType.ERROR).showAndWait());
            throw new RuntimeException("Failed to fetch the latest firmware URL: " + e.getMessage(), e);
        }
    }

    public Firmware next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
