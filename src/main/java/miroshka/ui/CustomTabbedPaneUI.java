package miroshka.ui;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class CustomTabbedPaneUI extends BasicTabbedPaneUI {

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets = new Insets(5, 10, 5, 10);
        contentBorderInsets = new Insets(5, 5, 5, 5);
        tabInsets = new Insets(8, 20, 8, 20);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(isSelected ? new Color(100, 150, 220) : new Color(45, 45, 45));
        g.fillRoundRect(x, y, w, h, 12, 12);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(isSelected ? new Color(120, 180, 255) : new Color(80, 80, 80));
        g.drawRoundRect(x, y, w, h, 12, 12);
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        g.setColor(new Color(80, 80, 80));
        g.drawRect(0, 0, tabPane.getWidth() - 1, tabPane.getHeight() - 1);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        g.setFont(font);
        g.setColor(isSelected ? Color.WHITE : new Color(180, 180, 180));
        int textX = textRect.x;
        int textY = textRect.y + metrics.getAscent();
        g.drawString(title, textX, textY);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }
}
