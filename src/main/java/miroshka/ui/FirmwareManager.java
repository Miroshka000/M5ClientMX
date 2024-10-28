package miroshka.ui;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import miroshka.downloader.FileDownloader;
import miroshka.installer.DependencyInstaller;
import miroshka.serial.SerialPortHandler;

public class FirmwareManager {
    private String currentFirmware = "CatHack";
    private LanguageManager languageManager;
    private M5ClientUI ui;

    public FirmwareManager(LanguageManager languageManager, M5ClientUI ui) {
        this.languageManager = languageManager;
        this.ui = ui;
    }

    public String[] getAvailablePorts() {
        return SerialPortHandler.getAvailablePorts().toArray(new String[0]);
    }

    public void installFirmware(JLabel statusLabel, JComboBox<String> comPortCombo) {
        String selectedPort = (String) comPortCombo.getSelectedItem();
        if (selectedPort == null || selectedPort.isEmpty()) {
            statusLabel.setText(languageManager.getText("status.noPort"));
            ui.appendToConsole("No port selected.");
            return;
        }

        DependencyInstaller.installDependencies(statusLabel, languageManager);

        File firmwareFile = new File("latest_firmware.bin");
        statusLabel.setText(languageManager.getText("status.installing"));
        ui.appendToConsole("Starting firmware installation...");

        new Thread(() -> {
            try {
                if (!firmwareFile.exists()) {
                    statusLabel.setText(languageManager.getText("status.downloading"));
                    ui.appendToConsole("Downloading firmware...");
                    FileDownloader.downloadFile(getFirmwareURL(), firmwareFile);
                }
                statusLabel.setText(languageManager.getText("status.flashing"));
                ui.appendToConsole("Flashing firmware...");
                boolean success = DependencyInstaller.flashFirmware(firmwareFile, selectedPort, ui);
                String resultMessage = success ? languageManager.getText("status.success") : languageManager.getText("status.failure");
                statusLabel.setText(resultMessage);
                ui.appendToConsole(resultMessage);
            } catch (IOException e) {
                String errorMessage = languageManager.getText("status.error") + e.getMessage();
                statusLabel.setText(errorMessage);
                ui.appendToConsole("Error: " + errorMessage);
            }
        }).start();
    }

    public void switchFirmware(JLabel firmwareImageLabel, JLabel statusLabel) {
        if ("CatHack".equals(currentFirmware)) {
            currentFirmware = "Bruce";
            firmwareImageLabel.setIcon(new ImageIcon(getClass().getResource("/images/bruce.png")));
        } else if ("Bruce".equals(currentFirmware)) {
            currentFirmware = "Nemo";
            firmwareImageLabel.setIcon(new ImageIcon(getClass().getResource("/images/nemo.png")));
        } else {
            currentFirmware = "CatHack";
            firmwareImageLabel.setIcon(new ImageIcon(getClass().getResource("/images/cathack.png")));
        }
        statusLabel.setText(languageManager.getText("status.switched") + currentFirmware);
    }

    private String getFirmwareURL() {
        return switch (currentFirmware) {
            case "Bruce" -> "https://github.com/pr3y/Bruce/releases/download/1.6.2/Bruce-m5stack-cplus2.bin";
            case "Nemo" -> "https://github.com/n0xa/m5stick-nemo/releases/download/v2.7.0/M5Nemo-v2.7.0-M5StickCPlus2.bin";
            default -> "https://github.com/Stachugit/CatHack/releases/download/CatHackv1.3_IRfix/CatHack_v1.3.bin";
        };
    }
}
