package miroshka;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import miroshka.config.ConfigManager;
import miroshka.controller.UIController;
import miroshka.lang.LangManager;
import miroshka.model.SerialPortUtils;

import java.util.Locale;


public class Main extends Application {

    private ConfigManager configManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeApp();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/miroshka/view/MainUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/miroshka/styles/style.css").toExternalForm());

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/miroshka/icon.png")));
            scene.setFill(Color.TRANSPARENT);
            primaryStage.setScene(scene);

            UIController controller = fxmlLoader.getController();
            controller.setLangManager(new LangManager());
            controller.setConfigManager(configManager);
            controller.setStage(primaryStage);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeApp() {
        configManager = new ConfigManager();
        String savedLanguage = configManager.getConfigValue("language", "ru");
        Locale.setDefault(new Locale(savedLanguage));
        SerialPortUtils.startMonitoringPorts();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
