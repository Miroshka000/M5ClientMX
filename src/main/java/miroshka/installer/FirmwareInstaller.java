package miroshka.installer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import miroshka.lang.LangManager;
import miroshka.view.CustomAlert;
import miroshka.model.FileDownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirmwareInstaller {

    private static final Pattern PROGRESS_PATTERN = Pattern.compile("\\((\\d+)%\\)");

    public static void flashFirmware(File firmwareFile, String comPort, ProgressBar progressBar, LangManager langManager, String firmwareUrl) throws IOException, InterruptedException {
        System.out.println(langManager.getTranslation("preparing_install_firmware"));

        if (firmwareFile.exists()) {
            boolean deleted = firmwareFile.delete();
            if (deleted) {
                System.out.println(langManager.getTranslation("firmware_file_deleted"));
            } else {
                System.out.println(langManager.getTranslation("failed_to_delete_firmware_file"));
            }
        }

        System.out.println(langManager.getTranslation("downloading_firmware"));
        FileDownloader.downloadFileWithProgress(firmwareUrl, firmwareFile, (progress, total) -> {
            Platform.runLater(() -> {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(progress);
            });
        });

        String esptoolPath = new File("esptool/esptool-win64/esptool.exe").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(esptoolPath, "--port", comPort, "--baud", "1500000", "write_flash", "0x00000", firmwareFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        Task<Void> flashingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("FLASH: " + line);

                        Matcher matcher = PROGRESS_PATTERN.matcher(line);
                        if (matcher.find()) {
                            int progress = Integer.parseInt(matcher.group(1));
                            Platform.runLater(() -> {
                                progressBar.progressProperty().unbind();
                                progressBar.setProgress(progress / 100.0);
                            });
                        }
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    Platform.runLater(() -> new CustomAlert(langManager.getTranslation("error"),
                            langManager.getTranslation("firmware_installation_failed"),
                            CustomAlert.AlertType.ERROR).showAndWait());
                } else {
                    Platform.runLater(() -> {
                        System.out.println(langManager.getTranslation("firmware_installed_successfully"));
                        new CustomAlert(langManager.getTranslation("success"),
                                langManager.getTranslation("firmware_installed_successfully"),
                                CustomAlert.AlertType.INFORMATION).showAndWait();

                        if (firmwareFile.exists()) {
                            boolean deleted = firmwareFile.delete();
                            if (deleted) {
                                System.out.println(langManager.getTranslation("firmware_file_deleted"));
                            } else {
                                System.out.println(langManager.getTranslation("failed_to_delete_firmware_file"));
                            }
                        }
                    });
                }
                return null;
            }
        };

        new Thread(flashingTask).start();
    }
}
