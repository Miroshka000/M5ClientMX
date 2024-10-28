package miroshka.ui;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import miroshka.downloader.FileDownloader;

public class DriverManager {
    private LanguageManager languageManager;

    public DriverManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public void installDriver(JLabel statusLabel) {
        statusLabel.setText(languageManager.getText("status.installingDriver"));
        try {
            File driverFile = new File("CH341SER.EXE");
            FileDownloader.downloadFile("https://github.com/Teapot321/M5Client/raw/refs/heads/main/CH341SER.EXE", driverFile);
            Runtime.getRuntime().exec(driverFile.getAbsolutePath());
            statusLabel.setText(languageManager.getText("status.driverSuccess"));
        } catch (IOException e) {
            statusLabel.setText(languageManager.getText("status.driverError") + e.getMessage());
        }
    }
}
