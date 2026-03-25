package view.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * RoundedPanel clips its content to a rounded rectangle shape
 * 
 * it uses a JLayeredPane with two layers
 * the DEFAULT layer holds the actual content panel
 * the PALETTE layer holds a transparent overlay that paints the rounded border
 * 
 * the clip region is applied in paintComponent so the content never
 * draws outside the rounded corners regardless of what it contains
 */
public class RoundedPanel extends JLayeredPane {

    // corner radius in pixels
    private int radius;

    // the content panel placed inside the rounded clip
    private JPanel content;

    /**
     * creates the rounded panel wrapping the given content
     * 
     * @param content the panel to clip to rounded corners
     * @param radius  the corner arc radius in pixels
     */
    public RoundedPanel(JPanel content, int radius) {
        this.radius = radius;
        setOpaque(false);
        this.content = content;

        // set initial bounds - will be overridden by doLayout
        this.content.setBounds(0, 0, content.getPreferredSize().width, content.getPreferredSize().height);
        add(this.content, JLayeredPane.DEFAULT_LAYER);

        // transparent overlay panel that draws the rounded border on top of the content
        RoundedBorder overlayBorder = new RoundedBorder(radius);
        JPanel overlay = new JPanel();
        overlay.setOpaque(false);
        overlay.setBorder(overlayBorder);
        overlay.setBounds(0, 0, content.getPreferredSize().width, content.getPreferredSize().height + 1);
        add(overlay, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * resizes all child components to fill this panel on every layout pass
     * needed because JLayeredPane does not have its own layout manager
     */
    @Override
    public void doLayout() {
        super.doLayout();
        for (Component c : getComponents()) {
            c.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * applies a rounded clip before painting so all child content
     * is visually contained within the rounded rectangle
     * 
     * @param g the graphics context
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
        super.paint(g2);
        g2.dispose();
    }
}
