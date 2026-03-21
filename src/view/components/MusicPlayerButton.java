package view.components;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Class for the music player button
 */
public class MusicPlayerButton extends JButton {

    /**
     * Constructor for the MusicPlayerButton class
     * @param icon
     */
    public MusicPlayerButton(Icon icon) {
        super(icon);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }
    public MusicPlayerButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    /**
     * paints the button
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getModel().isRollover()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0, 40));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
        }
    }
}