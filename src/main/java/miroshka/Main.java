package miroshka;

import miroshka.ui.CustomWindowFrame;
import miroshka.ui.M5ClientUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            M5ClientUI clientUI = new M5ClientUI();
            CustomWindowFrame windowFrame = new CustomWindowFrame(clientUI);
            windowFrame.setVisible(true);
        });
    }
}
