package miroshka.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlert {

    private Stage dialogStage;
    private Label messageLabel;
    private Label titleLabel;
    private Button closeButton;

    public enum AlertType {
        INFORMATION, WARNING, ERROR
    }

    public CustomAlert(String title, String message, AlertType alertType) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ff8e19; -fx-font-family: 'Born2bSporty';");

        messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-font-family: 'Born2bSporty';");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);

        closeButton = new Button("OK");
        closeButton.setStyle("-fx-background-color: #ff8e19; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        closeButton.setOnAction(event -> dialogStage.close());

        VBox vbox = new VBox(10, titleLabel, messageLabel, closeButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #333333; -fx-border-color: #ff8e19; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 20px;");

        Scene scene = new Scene(vbox, 300, 200);
        dialogStage.setScene(scene);

        applyAlertTypeStyle(alertType);
    }

    private void applyAlertTypeStyle(AlertType alertType) {
        switch (alertType) {
            case INFORMATION:
                titleLabel.setTextFill(javafx.scene.paint.Color.CORNFLOWERBLUE);
                break;
            case WARNING:
                titleLabel.setTextFill(javafx.scene.paint.Color.ORANGE);
                break;
            case ERROR:
                titleLabel.setTextFill(javafx.scene.paint.Color.RED);
                break;
        }
    }

    public void showAndWait() {
        dialogStage.showAndWait();
    }
}
