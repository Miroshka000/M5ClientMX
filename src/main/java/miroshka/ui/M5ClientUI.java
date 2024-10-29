package miroshka.ui;

import miroshka.installer.DependencyInstaller;
import miroshka.serial.StickInfoReader;
import miroshka.ui.manager.DriverManager;
import miroshka.ui.manager.FirmwareManager;
import miroshka.ui.manager.LanguageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class M5ClientUI extends JPanel {
    private JLabel statusLabel;
    private JLabel firmwareImageLabel;
    private JButton installButton, driverButton, switchFirmwareButton, languageButton, consoleButton;
    private FirmwareManager firmwareManager;
    private DriverManager driverManager;
    private LanguageManager languageManager;
    private JTextArea consoleTextArea, deviceInfoArea;
    private JPanel consolePanel;
    private String selectedPort;
    private Font customFont;

    public M5ClientUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
        loadCustomFont();
        initializeManagers();
        initializeUI();
        setupConsoleWindow();

        loadDeviceInfoAsync();
    }

    private void initializeManagers() {
        languageManager = new LanguageManager();
        driverManager = new DriverManager(languageManager);
        firmwareManager = new FirmwareManager(languageManager, this, firmwareImageLabel = new JLabel());
        DependencyInstaller.installDependencies(new JLabel(), languageManager);
    }

    private void initializeUI() {
        selectedPort = promptForPort();
        if (selectedPort == null) exitApplication("No COM port selected. The application will exit.");

        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadCustomFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/Born2bSportyV2.ttf")) {
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN, 14);
        }
    }

    private String promptForPort() {
        String[] availablePorts = firmwareManager.getAvailablePorts();
        if (availablePorts.length == 0) exitApplication("No COM ports available. Connect your device and try again.");
        return (String) JOptionPane.showInputDialog(this, "Select COM Port:", "Port Selection", JOptionPane.QUESTION_MESSAGE, null, availablePorts, availablePorts[0]);
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new CustomTabbedPaneUI());
        tabbedPane.setBackground(new Color(45, 45, 45));

        JPanel infoTab = createInfoTab();
        JPanel firmwareTab = createFirmwareTab();

        tabbedPane.addTab(languageManager.getText("tab.info"), infoTab);
        tabbedPane.addTab(languageManager.getText("tab.firmware"), firmwareTab);
        return tabbedPane;
    }

    private JPanel createInfoTab() {
        JPanel infoTab = createStyledPanel(new BorderLayout(), 10);

        deviceInfoArea = createStyledTextArea("Loading device info...");
        JPanel deviceInfoPanel = createStyledPanel(new BorderLayout(), 0);
        deviceInfoPanel.add(new JScrollPane(deviceInfoArea), BorderLayout.CENTER);
        deviceInfoPanel.setPreferredSize(new Dimension(250, infoTab.getHeight()));

        JPanel imagesPanel = createImagesPanel();
        infoTab.add(deviceInfoPanel, BorderLayout.WEST);
        infoTab.add(imagesPanel, BorderLayout.CENTER);
        return infoTab;
    }

    private JPanel createImagesPanel() {
        JPanel imagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -50, 0));
        imagesPanel.setBackground(new Color(30, 30, 30));

        JLabel stickImageLabel = createScaledImageLabel("/images/img.png", 300, 400);
        JLabel typecImageLabel = createScaledImageLabel("/images/typec.png", 200, 90);

        imagesPanel.add(stickImageLabel);
        imagesPanel.add(typecImageLabel);
        return imagesPanel;
    }

    private JLabel createScaledImageLabel(String path, int width, int height) {
        return new JLabel(new ImageIcon(new ImageIcon(getClass().getResource(path)).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
    }

    private JPanel createFirmwareTab() {
        JPanel firmwareTab = createStyledPanel(new GridBagLayout(), 10);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        firmwareImageLabel.setIcon(new ImageIcon(getClass().getResource("/images/cathack.png")));
        addComponent(firmwareTab, firmwareImageLabel, gbc, 0, 0, 2);

        installButton = createStyledButton(languageManager.getText("button.install"), new Color(70, 130, 180), e -> firmwareManager.installFirmware(statusLabel, selectedPort));
        driverButton = createStyledButton(languageManager.getText("button.driver"), new Color(70, 180, 70), e -> driverManager.installDriver(statusLabel));
        switchFirmwareButton = createStyledButton(languageManager.getText("button.switchFirmware"), new Color(180, 130, 70), e -> firmwareManager.switchFirmware());
        languageButton = createStyledButton(languageManager.getText("button.language"), new Color(130, 100, 200), this::switchLanguage);
        consoleButton = createStyledButton(languageManager.getText("button.console"), new Color(100, 100, 200), e -> consolePanel.setVisible(!consolePanel.isVisible()));

        addComponent(firmwareTab, installButton, gbc, 0, 1, 1);
        addComponent(firmwareTab, driverButton, gbc, 1, 1, 1);
        addComponent(firmwareTab, switchFirmwareButton, gbc, 0, 2, 1);
        addComponent(firmwareTab, languageButton, gbc, 1, 2, 1);
        addComponent(firmwareTab, consoleButton, gbc, 0, 3, 1);

        statusLabel = createStyledLabel(languageManager.getText("status.ready"));
        addComponent(firmwareTab, statusLabel, gbc, 0, 4, 2);

        return firmwareTab;
    }

    private JPanel createStyledPanel(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        panel.setBackground(new Color(30, 30, 30));
        return panel;
    }

    private JTextArea createStyledTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(customFont.deriveFont(14f));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(30, 30, 30));
        textArea.setEditable(false);
        return textArea;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(customFont.deriveFont(Font.ITALIC, 16f));
        label.setForeground(new Color(200, 200, 200));
        return label;
    }

    private JButton createStyledButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 50));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(customFont.deriveFont(Font.BOLD, 16f));
        button.addActionListener(action);
        return button;
    }

    private void addComponent(JPanel panel, Component component, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        panel.add(component, gbc);
    }

    private void switchLanguage(java.awt.event.ActionEvent event) {
        languageManager.toggleLanguage();
        updateText();
    }

    private void updateText() {
        for (JButton button : new JButton[]{installButton, driverButton, switchFirmwareButton, languageButton, consoleButton}) {
            button.setText(languageManager.getText("button." + button.getText().toLowerCase()));
        }
        statusLabel.setText(languageManager.getText("status.ready"));
    }

    private void setupConsoleWindow() {
        consolePanel = new JPanel(new BorderLayout());
        consoleTextArea = createStyledTextArea("");
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        consolePanel.add(scrollPane, BorderLayout.CENTER);
        consolePanel.setVisible(false);
        add(consolePanel, BorderLayout.SOUTH);
    }

    private void loadDeviceInfoAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                new StickInfoReader(selectedPort, deviceInfoArea);
                return null;
            }
        };
        worker.execute();
    }

    public void appendToConsole(String message) {
        consoleTextArea.append(message + "\n");
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
    }

    private void exitApplication(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}
