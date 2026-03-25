package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import view.View;

/**
 * MusicPlayerToggleButton is a custom JToggleButton for the shuffle and repeat buttons
 * 
 * when the button is selected it draws a filled rounded background
 * and a border to visually indicate that the mode is active
 * when hovered without being selected it shows a lighter highlight
 */
public class MusicPlayerToggleButton extends JToggleButton {

    /**
     * creates the toggle button with an icon
     * removes all default Swing button decorations
     * 
     * @param icon the icon to display on the button
     */
    public MusicPlayerToggleButton(Icon icon) {
        super(icon);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    /**
     * paints the button with different visual states for selected and hover
     * 
     * selected: filled rounded background with a border outline
     * hovered:  lighter filled rounded background only
     * default:  no background drawn
     * 
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected()) {
            // filled background at low opacity
            g2.setColor(new Color(View.TEXT.getRGB() + 0x22000000, true));
            g2.setStroke(new BasicStroke(0f));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            // border outline to make it look like a pressed state
            g2.setColor(new Color(View.TEXT.getRGB() + 0x66000000, true));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
        } else if (getModel().isRollover()) {
            // subtle hover fill only
            g2.setColor(new Color(View.TEXT.getRGB() + 0x22000000, true));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }

        g2.dispose();
    }
}
