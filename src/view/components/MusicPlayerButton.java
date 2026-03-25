package view.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * MusicPlayerButton is a custom JButton styled for the music player UI
 * 
 * it removes all default Swing button decorations and paints
 * a subtle rounded highlight when the mouse hovers over it
 */
public class MusicPlayerButton extends JButton {

    // corner radius for the hover highlight rectangle
    private int radius;

    /**
     * creates a button with an icon and a default radius of 10
     * 
     * @param icon the icon to display on the button
     */
    public MusicPlayerButton(Icon icon) {
        super(icon);
        radius = 10;
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    /**
     * creates a button with a text label and a default radius of 10
     * 
     * @param text the text to display on the button
     */
    public MusicPlayerButton(String text) {
        super(text);
        radius = 10;
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    /**
     * creates a button with an icon at a specific size and corner radius
     * 
     * @param icon   the icon to display
     * @param width  preferred width in pixels
     * @param height preferred height in pixels
     * @param radius corner radius for the hover rectangle
     */
    public MusicPlayerButton(Icon icon, int width, int height, int radius) {
        super(icon);
        this.radius = radius;
        this.setPreferredSize(new Dimension(width, height));
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    /**
     * paints the button and adds a semi-transparent hover highlight
     * the highlight is only drawn when the mouse is over the button
     * 
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getModel().isRollover()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // white at low opacity gives a soft highlight on any background color
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }
}
