package miroshka.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class M5ClientUI extends JFrame {
    private JComboBox<String> comPortCombo;
    private JLabel statusLabel;
    private JLabel firmwareImageLabel;
    private JButton installButton, driverButton, switchFirmwareButton, languageButton, consoleButton;
    private FirmwareManager firmwareManager;
    private DriverManager driverManager;
    private LanguageManager languageManager;
    private JTextArea consoleTextArea;
    private JFrame consoleFrame;

    public M5ClientUI() {
        setTitle("M5Client - Firmware Installer");
        setSize(1820, 920);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        languageManager = new LanguageManager();
        firmwareManager = new FirmwareManager(languageManager, this);
        driverManager = new DriverManager(languageManager);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(45, 45, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel(languageManager.getText("title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        firmwareImageLabel = new JLabel(new ImageIcon(getClass().getResource("/images/cathack.png")));
        firmwareImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(firmwareImageLabel, gbc);

        comPortCombo = new JComboBox<>(firmwareManager.getAvailablePorts());
        comPortCombo.setPreferredSize(new Dimension(250, 30));
        comPortCombo.setBackground(Color.WHITE);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(comPortCombo, gbc);

        installButton = createStyledButton(languageManager.getText("button.install"), new Color(70, 130, 180), e -> firmwareManager.installFirmware(statusLabel, comPortCombo));
        driverButton = createStyledButton(languageManager.getText("button.driver"), new Color(70, 180, 70), e -> driverManager.installDriver(statusLabel));
        switchFirmwareButton = createStyledButton(languageManager.getText("button.switchFirmware"), new Color(180, 130, 70), e -> firmwareManager.switchFirmware(firmwareImageLabel, statusLabel));
        languageButton = createStyledButton(languageManager.getText("button.language"), new Color(130, 100, 200), e -> switchLanguage());

        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(installButton, gbc);

        gbc.gridx = 1;
        mainPanel.add(driverButton, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        mainPanel.add(switchFirmwareButton, gbc);

        gbc.gridx = 1;
        mainPanel.add(languageButton, gbc);

        consoleButton = createStyledButton(languageManager.getText("button.console"), new Color(100, 100, 100), e -> openConsole());
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(consoleButton, gbc);

        statusLabel = new JLabel(languageManager.getText("status.ready"), SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        statusLabel.setForeground(new Color(200, 200, 200));

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(statusLabel, gbc);

        JLabel authorsLabel = new JLabel("Author: Miroshka & Teapot321", SwingConstants.RIGHT);
        authorsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        authorsLabel.setForeground(new Color(200, 200, 200));

        gbc.gridy = 7;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(authorsLabel, gbc);

        add(mainPanel);
        setVisible(true);

        setupConsoleWindow();
    }

    private JButton createStyledButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(250, 100));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.addActionListener(action);
        return button;
    }

    private void switchLanguage() {
        languageManager.toggleLanguage();
        updateText();
    }

    private void updateText() {
        setTitle(languageManager.getText("title"));
        installButton.setText(languageManager.getText("button.install"));
        driverButton.setText(languageManager.getText("button.driver"));
        switchFirmwareButton.setText(languageManager.getText("button.switchFirmware"));
        languageButton.setText(languageManager.getText("button.language"));
        consoleButton.setText(languageManager.getText("button.console"));
        statusLabel.setText(languageManager.getText("status.ready"));
    }

    private void setupConsoleWindow() {
        consoleFrame = new JFrame(languageManager.getText("button.console"));
        consoleFrame.setSize(600, 400);
        consoleFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        consoleFrame.add(scrollPane);
    }

    private void openConsole() {
        consoleFrame.setVisible(true);
    }

    public void appendToConsole(String message) {
        consoleTextArea.append(message + "\n");
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
    }
}
