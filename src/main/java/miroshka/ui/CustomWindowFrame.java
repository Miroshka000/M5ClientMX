package miroshka.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomWindowFrame extends JFrame {
    private Point initialClick;

    public CustomWindowFrame(JPanel contentPanel) {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image backgroundImage = new ImageIcon(getClass().getResource("/images/window.png")).getImage();
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new LineBorder(new Color(255, 153, 50), 5));

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        titleBar.setOpaque(true);
        titleBar.setBackground(new Color(0, 0, 0));

        JLabel minimizeButton = createButton("/images/minimize_1.png", "/images/minimize_hover.png", "/images/minimize_down.png");
        JLabel closeButton = createButton("/images/close.png", "/images/close_hover_1.png", "/images/close_down.png");

        minimizeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.setPreferredSize(new Dimension(20, 20));

        minimizeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setState(JFrame.ICONIFIED);
            }
        });

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });

        titleBar.add(minimizeButton);
        titleBar.add(closeButton);

        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                setLocation(X, Y);
            }
        });

        backgroundPanel.add(titleBar, BorderLayout.NORTH);
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JLabel createButton(String defaultIconPath, String hoverIconPath, String pressedIconPath) {
        ImageIcon defaultIcon = new ImageIcon(new ImageIcon(getClass().getResource(defaultIconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ImageIcon hoverIcon = new ImageIcon(new ImageIcon(getClass().getResource(hoverIconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ImageIcon pressedIcon = new ImageIcon(new ImageIcon(getClass().getResource(pressedIconPath)).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

        JLabel button = new JLabel(defaultIcon);
        button.setOpaque(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setIcon(defaultIcon);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setIcon(defaultIcon);
            }
        });

        return button;
    }
}
