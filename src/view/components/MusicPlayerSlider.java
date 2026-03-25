package view.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import view.View;

/**
 * MusicPlayerSlider is a custom JSlider with a minimal flat style
 * 
 * it replaces the default OS slider look with a thin rounded track
 * and a small circular thumb that match the application theme colors
 * used for both the playback progress bar and the volume slider
 */
public class MusicPlayerSlider extends JSlider {

    /**
     * creates the slider with the given range and initial value
     * applies the custom UI immediately
     * 
     * @param min   minimum value
     * @param max   maximum value
     * @param value initial value
     */
    public MusicPlayerSlider(int min, int max, int value) {
        super(min, max, value);
        setOpaque(false);
        setUI(new MusicPlayerSliderUI(this));
    }

    /**
     * custom slider UI that paints a flat rounded track and a circular thumb
     */
    private class MusicPlayerSliderUI extends BasicSliderUI {

        /**
         * creates the UI for the given slider
         * 
         * @param b the slider this UI will render
         */
        public MusicPlayerSliderUI(JSlider b) {
            super(b);
        }

        /**
         * paints the track as two rounded rectangles
         * the full track is drawn in the text color
         * the filled portion up to the thumb is drawn in the accent color
         * 
         * @param g the graphics context
         */
        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle t = trackRect;
            int trackHeight = 4;
            int y = t.y + t.height / 2 - trackHeight / 2;

            // full background track
            g2.setColor(View.TEXT);
            g2.fillRoundRect(t.x, y, t.width, trackHeight, trackHeight, trackHeight);

            // filled portion from the start to the current thumb position
            int fillWidth = thumbRect.x - t.x + thumbRect.width / 2;
            g2.setColor(View.ACCENT);
            g2.fillRoundRect(t.x, y, fillWidth, trackHeight, trackHeight, trackHeight);

            g2.dispose();
        }

        /**
         * paints the thumb as a small filled circle in the accent color
         * 
         * @param g the graphics context
         */
        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = 12;
            int x = thumbRect.x + thumbRect.width / 2 - size / 2;
            int y = thumbRect.y + thumbRect.height / 2 - size / 2;
            g2.setColor(View.ACCENT);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        /**
         * sets the thumb hit area to a fixed 16x16 square
         * the visible circle is smaller but the click area is a bit larger for usability
         */
        @Override
        protected void calculateThumbSize() {
            super.calculateThumbSize();
            thumbRect.setSize(16, 16);
        }
    }
}
