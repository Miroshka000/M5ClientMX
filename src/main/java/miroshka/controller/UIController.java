package miroshka.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import miroshka.model.Firmware;
import miroshka.model.FileDownloader;
import miroshka.model.SerialPortUtils;
import miroshka.installer.DependencyInstaller;
import miroshka.view.CustomAlert;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

public class UIController {
    @FXML
    private Button installButton, driversButton, minimizeButton, closeButton;
    @FXML
    private ComboBox<String> deviceMenu, comPortMenu;
    @FXML
    private ComboBox<Firmware> firmwareMenu;
    @FXML
    private ImageView firmwareImageView, decorativeImageView1, decorativeImageView2;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label appTitleLabel;

    private Firmware currentFirmware = Firmware.CATHACK;

    @FXML
    public void initialize() {
        deviceMenu.getItems().addAll("Plus2", "Card", "Plus1");
        deviceMenu.getSelectionModel().select("Plus2");

        List<String> ports = SerialPortUtils.getAvailablePorts();
        comPortMenu.getItems().addAll(ports);
        if (!comPortMenu.getItems().isEmpty()) {
            comPortMenu.getSelectionModel().select(0);
        }

        firmwareMenu.getItems().addAll(Firmware.values());
        firmwareMenu.setValue(currentFirmware);

        decorativeImageView1.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/plus2.png")));
        decorativeImageView2.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/typec.png")));

        redirectSystemOutputToConsole();
        updateFirmwareUI();
        setAppTitle();

        Task<Void> installEsptoolTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                progressBar.setVisible(true);
                progressBar.setProgress(0);
                DependencyInstaller.installDependencies(this::updateProgress);
                Platform.runLater(() -> {
                    showCustomAlert("Success", "esptool installed successfully.", CustomAlert.AlertType.INFORMATION);
                    progressBar.setVisible(false);
                });
                return null;
            }
        };

        new Thread(installEsptoolTask).start();
        installEsptoolTask.setOnFailed(e -> Platform.runLater(() -> {
            showCustomAlert("Error", "esptool installation failed: " + installEsptoolTask.getException().getMessage(), CustomAlert.AlertType.ERROR);
            progressBar.setVisible(false);
        }));

        deviceMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            onDeviceMenuChange();
        });
    }

    @FXML
    private void openGithubLink() {
        try {
            URI githubUri = new URI("https://github.com/Miroshka000/M5ClientMX");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(githubUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAppTitle() {
        String appName = "M5ClientMX";
        String appVersion = System.getProperty("app.version", "unknown");
        appTitleLabel.setText(appName + " v" + appVersion);
    }

    @FXML
    private void onDeviceMenuChange() {
        String selectedDevice = deviceMenu.getValue();
        if ("Card".equals(selectedDevice)) {
            decorativeImageView1.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/card.png")));
            decorativeImageView1.setFitWidth(300.0);
            decorativeImageView1.setFitHeight(300.0);
            decorativeImageView1.setLayoutX(230.0);
            decorativeImageView1.setLayoutY(0.0);
            decorativeImageView2.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/typec.png")));
            decorativeImageView2.setFitWidth(150.0);
            decorativeImageView2.setFitHeight(50.0);
            decorativeImageView2.setLayoutX(550.0);
            decorativeImageView2.setLayoutY(66.0);
        } else {
            decorativeImageView1.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/plus2.png")));
            decorativeImageView1.setFitWidth(300.0);
            decorativeImageView1.setFitHeight(300.0);
            decorativeImageView1.setLayoutX(230.0);
            decorativeImageView1.setLayoutY(14.0);
            decorativeImageView2.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/typec.png")));
            decorativeImageView2.setFitWidth(150.0);
            decorativeImageView2.setFitHeight(50.0);
            decorativeImageView2.setLayoutX(486.0);
            decorativeImageView2.setLayoutY(139.0);
        }
    }

    @FXML
    private void onFirmwareMenuChange() {
        currentFirmware = firmwareMenu.getValue();
        updateFirmwareUI();
    }

    @FXML
    private void onInstallButtonClick() {
        installButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);

        String selectedDevice = deviceMenu.getValue();
        String comPort = comPortMenu.getValue();

        Task<Void> firmwareInstallTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String firmwareUrl = currentFirmware.getDownloadUrl(selectedDevice);
                    File firmwareFile = new File(System.getProperty("user.home"), "latest_firmware.bin");
                    FileDownloader.downloadFileWithProgress(firmwareUrl, firmwareFile, (bytesRead, totalBytes) -> {
                        double progress = totalBytes > 0 ? (double) bytesRead / totalBytes : 0;
                        updateProgress(progress, 1.0);
                    });
                    FirmwareInstaller.flashFirmware(firmwareFile, comPort, progressBar);
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> new CustomAlert("Error", "Firmware installation failed: " + e.getMessage(), CustomAlert.AlertType.ERROR).showAndWait());
                }
                return null;
            }
        };

        firmwareInstallTask.setOnFailed(e -> Platform.runLater(() -> {
            showCustomAlert("Error", "Firmware installation failed.", CustomAlert.AlertType.ERROR);
            progressBar.setVisible(false);
        }));

        progressBar.progressProperty().bind(firmwareInstallTask.progressProperty());
        new Thread(firmwareInstallTask).start();
        firmwareInstallTask.setOnSucceeded(e -> {
            installButton.setDisable(false);
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        });
    }

    @FXML
    private void onDriversButtonClick() {
        String selectedDevice = deviceMenu.getValue();
        new Thread(() -> {
            try {
                switch (selectedDevice) {
                    case "Card":
                        DependencyInstaller.installDriver("CH341");
                        break;
                    case "Plus1":
                        DependencyInstaller.installDriver("CDM");
                        break;
                    case "Plus2":
                        DependencyInstaller.installDriver("CH9102");
                        break;
                    default:
                        Platform.runLater(() -> showCustomAlert("Error", "Unknown device selected.", CustomAlert.AlertType.ERROR));
                        return;
                }
                Platform.runLater(() -> showCustomAlert("Success", "Driver installed successfully.", CustomAlert.AlertType.INFORMATION));
            } catch (IOException e) {
                Platform.runLater(() -> showCustomAlert("Error", "Driver installation failed: " + e.getMessage(), CustomAlert.AlertType.ERROR));
            }
        }).start();
    }

    private void updateFirmwareUI() {
        firmwareImageView.setImage(new Image(getClass().getResourceAsStream(currentFirmware.getImagePath())));
        updateButtonColors();
    }

    private void updateButtonColors() {
        String color = currentFirmware.getButtonColor();
        installButton.setStyle("-fx-background-color: " + color);
        comPortMenu.setStyle("-fx-background-color: " + color);
        deviceMenu.setStyle("-fx-background-color: " + color);
        driversButton.setStyle("-fx-background-color: " + color);
        firmwareMenu.setStyle("-fx-background-color: " + color);
    }

    private void showCustomAlert(String title, String message, CustomAlert.AlertType alertType) {
        CustomAlert customAlert = new CustomAlert(title, message, alertType);
        customAlert.showAndWait();
    }

    private void redirectSystemOutputToConsole() {
        PrintStream consoleStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> consoleOutput.appendText(String.valueOf((char) b)));
            }
        });
        System.setOut(consoleStream);
        System.setErr(consoleStream);
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
