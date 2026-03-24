package view.components;

import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import controller.TabbedPaneMouseListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import view.View;

public class MusicPlayerTabbedPane extends JTabbedPane {

    private int hoveredTab = -1;
    private final View view;

    public MusicPlayerTabbedPane(View view) {
        super(JTabbedPane.LEFT);

        this.view = view;

        setUI(new MusicPlayerTabbedPaneUI());
        setOpaque(false);
        setFocusable(false);

        TabbedPaneMouseListener ml = new TabbedPaneMouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);
    }

    public int getHoveredTab() {
        return hoveredTab;
    }
    public void setHoveredTab(int hoveredTab) {
        this.hoveredTab = hoveredTab;
    }

    @Override
    public Component getSelectedComponent() {
        RoundedPanel p = (RoundedPanel) super.getSelectedComponent();
        return p.getComponent(1);
    }

    public View getView() {
        return view;
    }

    @Override
    public void setSelectedIndex(int index) {
        if (index == 0 && view.isVisible()) {
            view.getPlayerPanel().getPlaylistsPanel().showList();
        }
        super.setSelectedIndex(index);
    }

    private class MusicPlayerTabbedPaneUI extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(4, 4, 4, 4);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabInsets = new Insets(15, 24, 15, 24);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
            return maxTabWidth + tabAreaInsets.left + tabAreaInsets.right + 16;
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            // total number of tabs and tab area height for vertical centering
            int tabCount = tabPane.getTabCount();
            if (tabCount == 0) return;

            int totalTabHeight = 0;
            for (int i = 0; i < tabCount; i++) {
                totalTabHeight += rects[i].height;
            }

            int areaHeight = tabPane.getHeight();
            int offsetY = (areaHeight - totalTabHeight) / 2;

            // shift all tab rects to center them vertically
            for (int i = 0; i < tabCount; i++) {
                rects[i].y = offsetY + i * rects[i].height;
            }

            super.paintTabArea(g, tabPlacement, selectedIndex);
        }

        @Override
        protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
                                int tabIndex, Rectangle iconRect, Rectangle textRect) {
            Rectangle rect = rects[tabIndex];
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean isSelected = tabIndex == tabPane.getSelectedIndex();
            boolean isHovered = tabIndex == hoveredTab;

            // selected background
            if (isSelected) {
                new RoundedBorder(20, 10).paintBorder(tabPane, g2, rect.x, rect.y, rect.width, rect.height);
            } else if (isHovered) {
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
            }

            // draw tab title centered in the rect
            String title = tabPane.getTitleAt(tabIndex);
            FontMetrics fm = g2.getFontMetrics(tabPane.getFont());
            int textX = rect.x + (rect.width - fm.stringWidth(title)) / 2;
            int textY = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;

            g2.setFont(tabPane.getFont());
            g2.setColor(isSelected ? View.TEXT : View.TEXT);
            g2.drawString(title, textX, textY);

            g2.dispose();
        }

        // suppress default borders and dividers
        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {}

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                           int tabIndex, Rectangle iconRect,
                                           Rectangle textRect, boolean isSelected) {}

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
    }
}
