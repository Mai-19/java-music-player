import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

public class ScrollingLabel extends JLabel implements ActionListener {
    private String fullText;
    private float offset = 0;
    private int pauseCount = 0;
    private static final int PAUSE_TICKS = 50;
    private static final float SCROLL_SPEED = 1f;
    private Timer timer = new Timer(30, this);
    private int textWidth;
    private String displayString;
    private int bareWidth;

    public ScrollingLabel() {
        super();
        int height = getFontMetrics(getFont()).getHeight();
        setPreferredSize(new Dimension(100, height));
        setMinimumSize(new Dimension(100, height));
        setMaximumSize(new Dimension(100, height));
        timer.start();
    }

    public void setScrollingText(String text) {
        displayString = text;
        fullText = text + "     ";
        offset = 0;
        pauseCount = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (fullText == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        textWidth = fm.stringWidth(fullText);
        bareWidth = fm.stringWidth(displayString);
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        if (bareWidth <= getWidth()) {
            g2.drawString(fullText, 0, y);
        } else {
            float x = -offset;
            g2.drawString(fullText, x, y);
            g2.drawString(fullText, x + textWidth, y);
        }
        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bareWidth <= getWidth()) return;
        if (offset == 0 && pauseCount < PAUSE_TICKS) {
            pauseCount++;
            return;
        }
        pauseCount = 0;
        offset += SCROLL_SPEED;
        if (offset >= textWidth) {
            offset = 0;
            pauseCount = 0;
        }
        repaint();
    }
}