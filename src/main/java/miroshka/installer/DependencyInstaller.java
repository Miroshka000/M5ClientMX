package miroshka.installer;

import miroshka.downloader.FileDownloader;
import miroshka.ui.manager.LanguageManager;
import miroshka.ui.M5ClientUI;

import javax.swing.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DependencyInstaller {
    private static final File CURRENT_DIR = new File(System.getProperty("user.dir"), "esptool");
    private static final File ESPTOOL_ZIP = new File(CURRENT_DIR, "esptool.zip");
    private static final File ESPTOOL_DIR = new File(CURRENT_DIR, "esptool-win64");
    private static final File ESPTOOL_EXECUTABLE = new File(ESPTOOL_DIR, "esptool.exe");

    private static final String ESPTOOL_URL = "https://github.com/espressif/esptool/releases/download/v4.8.1/esptool-v4.8.1-win64.zip";
    private static final String DRIVER_URL = "https://github.com/Teapot321/M5Client/raw/refs/heads/main/CH341SER.EXE";
    private static final File DRIVER_FILE = new File(CURRENT_DIR, "CH341SER.EXE");

    public static void installDependencies(JLabel statusLabel, LanguageManager languageManager) {
        try {
            statusLabel.setText(languageManager.getText("status.installingDependencies"));

            ensureEsptoolInstalled();

            if (!DRIVER_FILE.exists()) {
                statusLabel.setText(languageManager.getText("status.installingDriver"));
                FileDownloader.downloadFile(DRIVER_URL, DRIVER_FILE);
                Runtime.getRuntime().exec(DRIVER_FILE.getAbsolutePath());
            }

            statusLabel.setText(languageManager.getText("status.dependenciesInstalled"));
        } catch (IOException e) {
            statusLabel.setText(languageManager.getText("status.installationError") + e.getMessage());
        }
    }

    private static void ensureEsptoolInstalled() throws IOException {
        if (!ESPTOOL_EXECUTABLE.exists()) {
            System.out.println("Downloading esptool...");
            FileDownloader.downloadFile(ESPTOOL_URL, ESPTOOL_ZIP);

            if (!ESPTOOL_DIR.exists()) {
                ESPTOOL_DIR.mkdirs();
            }

            unzipEsptool(ESPTOOL_ZIP, ESPTOOL_DIR);
            ESPTOOL_ZIP.delete();

            if (!ESPTOOL_EXECUTABLE.exists()) {
                throw new FileNotFoundException("esptool.exe not found in " + ESPTOOL_DIR.getAbsolutePath());
            }
        }
    }

    private static void unzipEsptool(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName().substring(entry.getName().indexOf("/") + 1));
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

    public static boolean flashFirmware(File firmwareFile, String port, M5ClientUI ui) {
        try {
            ensureEsptoolInstalled();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    ESPTOOL_EXECUTABLE.getAbsolutePath(), "--port", port, "--baud", "115200", "write_flash", "0x00000", firmwareFile.getAbsolutePath()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    ui.appendToConsole(line);
                }
            }
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            ui.appendToConsole("Flashing failed: " + e.getMessage());
            return false;
        }
    }
}
