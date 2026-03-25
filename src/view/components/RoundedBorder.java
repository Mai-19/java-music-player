package view.components;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

import view.View;

/**
 * RoundedBorder is a custom Swing border that draws a rounded rectangle outline
 * 
 * used on panels that need a visible rounded edge such as the settings box
 * and the selected tab highlight in the tabbed pane
 * 
 * the border color is taken from View.ACCENT and the stroke width is fixed at 2px
 */
public class RoundedBorder extends AbstractBorder {

    // corner arc radius in pixels
    private int radius;

    // padding inside the border applied as insets
    private int inset;

    /**
     * creates a rounded border with default padding of 10
     * 
     * @param radius corner arc radius in pixels
     */
    public RoundedBorder(int radius) {
        super();
        this.radius = radius;
        this.inset = 10;
    }

    /**
     * creates a rounded border with custom padding
     * 
     * @param radius corner arc radius in pixels
     * @param insets padding in pixels applied equally on all sides
     */
    public RoundedBorder(int radius, int insets) {
        super();
        this.radius = radius;
        this.inset = insets;
    }

    /**
     * draws the rounded rectangle border around the component
     * 
     * @param c      the component this border is applied to
     * @param g      the graphics context
     * @param x      left edge of the border area
     * @param y      top edge of the border area
     * @param width  total width of the border area
     * @param height total height of the border area
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(View.ACCENT);
        g2.setStroke(new BasicStroke(2));
        // inset by 1 pixel so the stroke does not get clipped at the edges
        g2.draw(new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, radius, radius));
        g2.dispose();
    }

    /**
     * returns the insets that define the padding inside the border
     * 
     * @param c the component - not used
     * @return insets with equal padding on all sides
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(inset, inset, inset, inset);
    }
}
