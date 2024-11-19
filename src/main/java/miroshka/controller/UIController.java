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
import miroshka.config.ConfigManager;
import miroshka.installer.FirmwareInstaller;
import miroshka.lang.LangManager;
import miroshka.model.Firmware;
import miroshka.model.ProgressCallback;
import miroshka.model.SerialPortUtils;
import miroshka.installer.DependencyInstaller;
import miroshka.view.CustomAlert;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class UIController {
    @FXML
    private CheckBox devModeCheckBox;
    @FXML
    private Button installButton, driversButton, minimizeButton, closeButton, saveSettingsButton;
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
    private Label appTitleLabel, deviceLabel, portLabel, firmwareLabel, betaSectionLabel, languageLabel;
    @FXML
    private CheckBox autoUpdateCheckBox, notificationsCheckBox;
    @FXML
    private ComboBox<String> languageMenu;
    @FXML
    private Tab mainTab, settingsTab;
    @FXML
    private Hyperlink copyrightLink;

    private PrintStream fileStream;
    private boolean isDevModeEnabled = false;
    private Firmware currentFirmware = Firmware.CATHACK;
    private LangManager langManager;
    private ConfigManager configManager;
    private Stage primaryStage;

    @FXML
    public void initialize() {
        langManager = new LangManager();
        Firmware.setLangManager(langManager);

        setAppTitle();
        initializeLanguageMenu();
        langManager.setLocale(new Locale("ru"));
        setLocalizedTexts();

        initializeDeviceMenu();
        initializeComPortMenu();
        initializeFirmwareMenu();
        initializeImages();
        redirectSystemOutputToConsole();
        updateFirmwareUI();

        installEsptool();

        SerialPortUtils.setListener(updatedPorts -> Platform.runLater(() -> {
            comPortMenu.getItems().clear();
            comPortMenu.getItems().addAll(updatedPorts);
            if (!updatedPorts.isEmpty()) {
                comPortMenu.getSelectionModel().select(0);
            }
        }));
    }

    private void initializeDeviceMenu() {
        deviceMenu.getItems().addAll("Plus2", "Card", "Plus1");
        deviceMenu.getSelectionModel().select("Plus2");
        deviceMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> onDeviceMenuChange());
    }

    private void initializeComPortMenu() {
        List<String> ports = SerialPortUtils.getAvailablePorts();
        comPortMenu.getItems().addAll(ports);
        if (!comPortMenu.getItems().isEmpty()) {
            comPortMenu.getSelectionModel().select(0);
        }
    }

    private void initializeFirmwareMenu() {
        firmwareMenu.getItems().addAll(Firmware.values());
        firmwareMenu.setValue(currentFirmware);
    }

    private void initializeImages() {
        decorativeImageView1.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/plus2.png")));
        decorativeImageView2.setImage(new Image(getClass().getResourceAsStream("/miroshka/images/typec.png")));
    }

    private void installEsptool() {
        ProgressCallback progressCallback = (bytesRead, totalBytes) -> Platform.runLater(() -> {
            double progress = totalBytes > 0 ? (double) bytesRead / totalBytes : 0;
            progressBar.setProgress(progress);
        });

        Task<Void> installEsptoolTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                progressBar.setVisible(true);
                progressBar.setProgress(0);
                try {
                    DependencyInstaller.installDependencies(progressCallback, langManager);
                    Platform.runLater(() -> showCustomAlert(langManager.getTranslation("success"),
                            langManager.getTranslation("esptool_installed_successfully"),
                            CustomAlert.AlertType.INFORMATION));
                } catch (IOException e) {
                    Platform.runLater(() -> showCustomAlert(langManager.getTranslation("error"),
                            langManager.getTranslation("dependency_installation_failed") + ": " + e.getMessage(),
                            CustomAlert.AlertType.ERROR));
                } finally {
                    progressBar.setVisible(false);
                }
                return null;
            }
        };

        new Thread(installEsptoolTask).start();

        installEsptoolTask.setOnFailed(e -> Platform.runLater(() -> {
            showCustomAlert(langManager.getTranslation("error"),
                    langManager.getTranslation("esptool_installation_failed") + ": " + installEsptoolTask.getException().getMessage(),
                    CustomAlert.AlertType.ERROR);
            progressBar.setVisible(false);
        }));
    }

    private void initializeLanguageMenu() {
        languageMenu.getItems().addAll("Русский", "English");
        languageMenu.getSelectionModel().select("Русский");
        languageMenu.setOnAction(event -> onLanguageChange());
    }

    @FXML
    private void onDevModeToggle() {
        isDevModeEnabled = devModeCheckBox.isSelected();
        if (isDevModeEnabled) {
            startLoggingToFile();
        } else {
            stopLoggingToFile();
        }
    }

    private void startLoggingToFile() {
        try {
            String executableDir = System.getProperty("user.dir");

            File logFile = new File(executableDir + File.separator + "logs" + File.separator + "log.txt");

            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            fileStream = new PrintStream(new FileOutputStream(logFile, true), true, StandardCharsets.UTF_8);

            System.setOut(fileStream);
            System.setErr(fileStream);

            System.out.println("Dev Mode enabled: Logs will be saved to " + logFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopLoggingToFile() {
        if (fileStream != null) {
            fileStream.close();
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
            System.out.println("Dev Mode disabled: Logs will no longer be saved to log.txt");
        }
    }

    @FXML
    private void onLanguageChange() {
        String selectedLanguage = languageMenu.getValue();
        Locale locale;

        if ("Русский".equals(selectedLanguage)) {
            locale = new Locale("ru");
        } else {
            locale = new Locale("en");
        }

        langManager.setLocale(locale);

        configManager.setLanguage(locale.getLanguage());

        setLocalizedTexts();
    }

    public void setLangManager(LangManager langManager) {
        this.langManager = langManager;
    }

    private void setLocalizedTexts() {
        installButton.setText(langManager.getTranslation("install_button"));
        driversButton.setText(langManager.getTranslation("drivers_button"));
        saveSettingsButton.setText(langManager.getTranslation("save_settings_button"));

        deviceLabel.setText(langManager.getTranslation("device_label"));
        portLabel.setText(langManager.getTranslation("port_label"));
        firmwareLabel.setText(langManager.getTranslation("firmware_label"));
        betaSectionLabel.setText(langManager.getTranslation("beta_section"));
        languageLabel.setText(langManager.getTranslation("language_label"));

        mainTab.setText(langManager.getTranslation("main_tab"));
        settingsTab.setText(langManager.getTranslation("settings_tab"));

        autoUpdateCheckBox.setText(langManager.getTranslation("auto_update"));
        notificationsCheckBox.setText(langManager.getTranslation("notifications"));

        languageMenu.setPromptText(langManager.getTranslation("language_prompt"));
        deviceMenu.setPromptText(langManager.getTranslation("device_selection_prompt"));
        comPortMenu.setPromptText(langManager.getTranslation("port_selection_prompt"));
        firmwareMenu.setPromptText(langManager.getTranslation("firmware_selection_prompt"));

        copyrightLink.setText(langManager.getTranslation("github_link"));
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

                    FirmwareInstaller.flashFirmware(firmwareFile, comPort, progressBar, langManager, firmwareUrl);
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> showCustomAlert(langManager.getTranslation("error"),
                            langManager.getTranslation("firmware_installation_failed") + ": " + e.getMessage(),
                            CustomAlert.AlertType.ERROR));
                }
                return null;
            }
        };

        firmwareInstallTask.setOnFailed(e -> Platform.runLater(() -> {
            showCustomAlert(langManager.getTranslation("error"),
                    langManager.getTranslation("firmware_installation_failed"),
                    CustomAlert.AlertType.ERROR);
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
                        DependencyInstaller.installDriver("CH341", langManager);
                        break;
                    case "Plus1":
                        DependencyInstaller.installDriver("CH341", langManager);
                        break;
                    case "Plus2":
                        DependencyInstaller.installDriver("CH341", langManager);
                        break;
                    default:
                        Platform.runLater(() -> showCustomAlert(langManager.getTranslation("error"),
                                langManager.getTranslation("unknown_device_selected"),
                                CustomAlert.AlertType.ERROR));
                        return;
                }
                Platform.runLater(() -> showCustomAlert(langManager.getTranslation("success"),
                        langManager.getTranslation("driver_installed_successfully"),
                        CustomAlert.AlertType.INFORMATION));
            } catch (IOException e) {
                Platform.runLater(() -> showCustomAlert(langManager.getTranslation("error"),
                        langManager.getTranslation("driver_installation_failed") + ": " + e.getMessage(),
                        CustomAlert.AlertType.ERROR));
            }
        }).start();
    }

    private void updateFirmwareUI() {
        firmwareImageView.setImage(new Image(getClass().getResourceAsStream(currentFirmware.getImagePath())));
        updateButtonColors();
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
        }, true, StandardCharsets.UTF_8);

        System.setOut(consoleStream);
        System.setErr(consoleStream);
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setOnCloseRequest(event -> onWindowClose());
    }

    private void onWindowClose() {
        SerialPortUtils.stopMonitoringPorts();
        if (configManager != null) {
            configManager.saveConfig();
        }
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void closeWindow() {
        onWindowClose();
        Platform.exit();
        System.exit(0);
    }

    private void updateButtonColors() {
        String color = currentFirmware.getButtonColor();
        installButton.setStyle("-fx-background-color: " + color);
        comPortMenu.setStyle("-fx-background-color: " + color);
        deviceMenu.setStyle("-fx-background-color: " + color);
        driversButton.setStyle("-fx-background-color: " + color);
        firmwareMenu.setStyle("-fx-background-color: " + color);
    }

    @FXML
    private void saveConfig() {
        String selectedLanguage = languageMenu.getValue();
        String languageCode = selectedLanguage.equals("Русский") ? "ru" : "en";

        configManager.setConfigValue("language", languageCode);
        configManager.setConfigValue("devMode", String.valueOf(devModeCheckBox.isSelected()));
        configManager.setConfigValue("autoUpdate", String.valueOf(autoUpdateCheckBox.isSelected()));
        configManager.setConfigValue("notifications", String.valueOf(notificationsCheckBox.isSelected()));
        configManager.setConfigValue("device", deviceMenu.getValue());
        configManager.setConfigValue("comPort", comPortMenu.getValue());

        configManager.saveConfig();

        showCustomAlert("Настройки", "Настройки успешно сохранены.", CustomAlert.AlertType.INFORMATION);
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

}
