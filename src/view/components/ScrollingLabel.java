package view.components;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * ScrollingLabel is a JLabel that scrolls its text horizontally when it is too wide to fit
 * 
 * if the text fits within the component width it is drawn statically
 * if the text is wider than the component the text scrolls from right to left on a loop
 * 
 * a brief pause is inserted at the start of each scroll cycle before the text begins moving
 * the scroll is driven by a Swing Timer that fires every 30ms
 */
public class ScrollingLabel extends JLabel implements ActionListener {

    // the full text plus trailing spaces used as the looping scroll string
    private String fullText;

    // the original text without trailing spaces used to measure the bare width
    private String displayString;

    // current horizontal scroll offset in pixels
    private float offset = 0;

    // number of ticks spent paused at the start before scrolling begins
    private int pauseCount = 0;

    // how many ticks to pause before scrolling starts each cycle
    private static final int PAUSE_TICKS = 50;

    // pixels to advance per timer tick
    private static final float SCROLL_SPEED = 1f;

    // Swing timer that drives the scroll animation
    private Timer timer = new Timer(30, this);

    // pixel width of the full looping string including trailing spaces
    private int textWidth;

    // pixel width of just the display text without trailing spaces
    private int bareWidth;

    /**
     * creates the scrolling label with a fixed height and minimum width
     * starts the scroll timer immediately
     */
    public ScrollingLabel() {
        super();
        int height = getFontMetrics(getFont()).getHeight();
        setPreferredSize(new Dimension(100, height));
        setMinimumSize(new Dimension(100, height));
        setMaximumSize(new Dimension(100, height));
        timer.start();
    }

    /**
     * sets the text to display and resets the scroll animation to the beginning
     * 
     * @param text the text to display and potentially scroll
     */
    public void setScrollingText(String text) {
        displayString = text;
        // trailing spaces create a gap before the text loops back to the start
        fullText = text + "     ";
        offset = 0;
        pauseCount = 0;
        repaint();
    }

    /**
     * draws the label text - scrolls it if it is wider than the component
     * 
     * when scrolling two copies of the text are drawn side by side
     * so the transition from the end back to the start is seamless
     * 
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (fullText == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        textWidth = fm.stringWidth(fullText);
        bareWidth  = fm.stringWidth(displayString);
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        if (bareWidth <= getWidth()) {
            // text fits - draw it statically
            g2.drawString(fullText, 0, y);
        } else {
            // text is too wide - draw it twice offset by textWidth for seamless looping
            float x = -offset;
            g2.drawString(fullText, x, y);
            g2.drawString(fullText, x + textWidth, y);
        }

        g2.dispose();
    }

    /**
     * called every 30ms by the scroll timer
     * 
     * does nothing if the text fits without scrolling
     * otherwise advances the scroll offset and repaints
     * resets the offset when the text has scrolled one full width
     * 
     * @param e the timer event - not used directly
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (bareWidth <= getWidth()) return;

        // pause at the start before scrolling begins
        if (offset == 0 && pauseCount < PAUSE_TICKS) {
            pauseCount++;
            return;
        }

        pauseCount = 0;
        offset += SCROLL_SPEED;

        // wrap back to the start once a full scroll cycle completes
        if (offset >= textWidth) {
            offset = 0;
            pauseCount = 0;
        }

        repaint();
    }
}
