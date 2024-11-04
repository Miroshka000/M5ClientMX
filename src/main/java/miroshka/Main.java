package miroshka;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import miroshka.controller.UIController;
import miroshka.lang.LangManager;

import java.util.Locale;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Locale.setDefault(new Locale("ru"));
            LangManager langManager = new LangManager();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/miroshka/view/MainUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/miroshka/styles/style.css").toExternalForm());

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/miroshka/icon.png")));
            scene.setFill(Color.TRANSPARENT);
            primaryStage.setScene(scene);

            UIController controller = fxmlLoader.getController();
            controller.setLangManager(langManager);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
