package miroshka.installer;

import javafx.application.Platform;
import miroshka.model.FileDownloader;
import miroshka.model.ProgressCallback;
import miroshka.view.CustomAlert;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DependencyInstaller {
    private static final File CURRENT_DIR = new File(System.getProperty("user.dir"), "esptool");
    private static final File ESPTOOL_ZIP = new File(CURRENT_DIR, "esptool.zip");
    private static final File ESPTOOL_DIR = new File(CURRENT_DIR, "esptool-win64");
    private static final File ESPTOOL_EXECUTABLE = new File(ESPTOOL_DIR, "esptool.exe");

    private static final String ESPTOOL_URL = "https://github.com/espressif/esptool/releases/download/v4.8.1/esptool-v4.8.1-win64.zip";
    private static final String DRIVER_BASE_URL = "https://github.com/Miroshka000/M5ClientMX/raw/main/Drivers/";

    private static final File DRIVER_CDM = new File(CURRENT_DIR, "CDM212364.exe");
    private static final File DRIVER_CH341 = new File(CURRENT_DIR, "CH341SER.EXE");
    private static final File DRIVER_CH9102 = new File(CURRENT_DIR, "CH9102.exe");

    public static void installDependencies(ProgressCallback progressCallback) throws IOException {
        try {
            ensureEsptoolInstalled(progressCallback);
        } catch (IOException e) {
            Platform.runLater(() -> new CustomAlert("Error", "Dependency installation failed: " + e.getMessage(), CustomAlert.AlertType.ERROR).showAndWait());
            throw e;
        }
    }

    private static void ensureEsptoolInstalled(ProgressCallback progressCallback) throws IOException {
        if (!ESPTOOL_EXECUTABLE.exists()) {
            System.out.println("Esptool executable not found. Starting installation...");

            if (!CURRENT_DIR.exists()) {
                CURRENT_DIR.mkdirs();
            }

            System.out.println("Downloading esptool...");
            FileDownloader.downloadFileWithProgress(ESPTOOL_URL, ESPTOOL_ZIP, progressCallback);

            if (!ESPTOOL_ZIP.exists() || ESPTOOL_ZIP.length() == 0) {
                Platform.runLater(() -> new CustomAlert("Error", "Failed to download esptool.zip. Please check your internet connection or the download URL.", CustomAlert.AlertType.ERROR).showAndWait());
                throw new FileNotFoundException("Failed to download esptool.zip.");
            }

            System.out.println("Unzipping esptool...");
            unzipEsptool(ESPTOOL_ZIP, ESPTOOL_DIR);

            if (ESPTOOL_ZIP.exists()) {
                ESPTOOL_ZIP.delete();
            }

            if (!ESPTOOL_EXECUTABLE.exists()) {
                Platform.runLater(() -> new CustomAlert("Error", "esptool.exe not found in " + ESPTOOL_DIR.getAbsolutePath(), CustomAlert.AlertType.ERROR).showAndWait());
                throw new FileNotFoundException("esptool.exe not found.");
            }

            System.out.println("Esptool installed successfully.");
        } else {
            System.out.println("Esptool is already installed.");
        }
    }

    public static void installDriver(String driverType) throws IOException {
        try {
            File driverFile;
            switch (driverType) {
                case "CDM":
                    driverFile = DRIVER_CDM;
                    break;
                case "CH341":
                    driverFile = DRIVER_CH341;
                    break;
                case "CH9102":
                    driverFile = DRIVER_CH9102;
                    break;
                default:
                    Platform.runLater(() -> new CustomAlert("Error", "Unknown driver type: " + driverType, CustomAlert.AlertType.ERROR).showAndWait());
                    throw new IllegalArgumentException("Unknown driver type: " + driverType);
            }
            ensureDriverInstalled(driverFile, driverFile.getName());
        } catch (IOException e) {
            Platform.runLater(() -> new CustomAlert("Error", "Driver installation failed: " + e.getMessage(), CustomAlert.AlertType.ERROR).showAndWait());
            throw e;
        }
    }

    private static void ensureDriverInstalled(File driverFile, String driverName) throws IOException {
        if (!driverFile.exists()) {
            String driverUrl = DRIVER_BASE_URL + driverName;
            FileDownloader.downloadFileWithProgress(driverUrl, driverFile, null);
            new ProcessBuilder(driverFile.getAbsolutePath()).start();
        }
    }

    private static void unzipEsptool(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            boolean skipFirstDir = true;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (skipFirstDir && entryName.startsWith("esptool-win64/")) {
                    entryName = entryName.substring("esptool-win64/".length());
                } else {
                    skipFirstDir = false;
                }

                File newFile = new File(destDir, entryName);
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
