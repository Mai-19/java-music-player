package view.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import view.View;

/**
 * LyricsPanel displays the lyrics for the currently playing song
 *
 * supports both synced LRC files where each line highlights as the song plays
 * and unsynced files where all lines are shown at once
 *
 * the active line is displayed larger and brighter than the rest
 * the panel smoothly scrolls to keep the active line centered
 */
public class LyricsPanel extends JPanel {

    // ordered list of (timestamp ms, line text) pairs built from the TreeMap
    private List<Long> timestamps;
    private List<String> lines;

    // index of the currently active line
    private int activeIndex = -1;

    // the panel that holds all lyric line labels
    private JPanel linesPanel;
    private JScrollPane scrollPane;

    private List<JPanel> rowPanels;
    private List<JLabel> rowLabels;

    // smooth scroll state
    private Timer scrollTimer;
    private int scrollTarget = 0;
    private static final int SCROLL_STEP = 8;   // px per timer tick
    private static final int SCROLL_DELAY = 12; // ms per tick

    // font sizes
    private static final float ACTIVE_SIZE   = 25f;
    private static final float INACTIVE_SIZE = 14f;

    public LyricsPanel() {
        super(new BorderLayout());

        setOpaque(true);

        timestamps  = new ArrayList<>();
        lines       = new ArrayList<>();
        rowPanels   = new ArrayList<>();
        rowLabels   = new ArrayList<>();

        linesPanel = new JPanel();
        linesPanel.setLayout(new BoxLayout(linesPanel, BoxLayout.Y_AXIS));
        linesPanel.setOpaque(false);
        linesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(linesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(View.BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        scrollTimer = new Timer(SCROLL_DELAY, e -> animateScroll());
        scrollTimer.setRepeats(true);

        showNoLyrics();
    }

    public void setLyrics(TreeMap<Long, String> lyricsMap) {
        timestamps.clear();
        lines.clear();
        activeIndex = -1;

        if (lyricsMap == null || lyricsMap.isEmpty()) {
            showNoLyrics();
            return;
        }

        for (var entry : lyricsMap.entrySet()) {
            timestamps.add(entry.getKey());
            lines.add(entry.getValue());
        }

        rebuildLines();
    }

    public void updatePosition(int positionSeconds) {
        if (timestamps.isEmpty()) return;

        long ms = (long) positionSeconds * 1000;
        int newActive = -1;

        // find the last timestamp that is <= current position
        for (int i = timestamps.size() - 1; i >= 0; i--) {
            if (timestamps.get(i) <= ms) {
                newActive = i;
                break;
            }
        }

        if (newActive != activeIndex) {
            activeIndex = newActive;
            refreshStyles();
            scrollToActive();
        }
    }

    private void showNoLyrics() {
        linesPanel.removeAll();
        rowPanels.clear();
        rowLabels.clear();

        JLabel msg = new JLabel("No lyrics available", SwingConstants.CENTER);
        msg.setForeground(View.ACCENT);
        msg.setFont(msg.getFont().deriveFont(Font.ITALIC, 15f));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        linesPanel.add(Box.createVerticalGlue());
        linesPanel.add(msg);
        linesPanel.add(Box.createVerticalGlue());

        linesPanel.revalidate();
        linesPanel.repaint();
    }

    private void rebuildLines() {
        linesPanel.removeAll();
        rowPanels.clear();
        rowLabels.clear();

        // top padding so the first line isn't flush against the top
        linesPanel.add(Box.createVerticalStrut(getHeight() / 2));

        for (int i = 0; i < lines.size(); i++) {
            JLabel label = new JLabel(lines.get(i), SwingConstants.CENTER);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setFont(label.getFont().deriveFont(INACTIVE_SIZE));
            label.setForeground(View.ACCENT);

            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 32));
            row.add(Box.createHorizontalGlue());
            row.add(label);
            row.add(Box.createHorizontalGlue());

            rowPanels.add(row);
            rowLabels.add(label);
            linesPanel.add(row);
        }

        // bottom padding to match top
        linesPanel.add(Box.createVerticalStrut(getHeight() / 2));

        scrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
        scrollTarget = 0;

        linesPanel.revalidate();
        linesPanel.repaint();
    }

    private void refreshStyles() {
        for (int i = 0; i < rowLabels.size(); i++) {
            JLabel label = rowLabels.get(i);
            if (i == activeIndex) {
                label.setForeground(View.TEXT);
                label.setFont(label.getFont().deriveFont(Font.BOLD, ACTIVE_SIZE));
            } else {
                // fade lines further from active even more
                label.setForeground(View.ACCENT);
                label.setFont(label.getFont().deriveFont(Font.PLAIN, INACTIVE_SIZE));
            }
        }
        linesPanel.revalidate();
        linesPanel.repaint();
    }

    private void scrollToActive() {
        if (activeIndex < 0 || activeIndex >= rowPanels.size()) return;

        SwingUtilities.invokeLater(() -> {
            JPanel row = rowPanels.get(activeIndex);
            int rowCenter = row.getY() + row.getHeight() / 2;
            int viewCenter = scrollPane.getViewport().getHeight() / 2;
            scrollTarget = Math.max(0, rowCenter - viewCenter);
            if (!scrollTimer.isRunning()) scrollTimer.start();
        });
    }

    private void animateScroll() {
        int current = scrollPane.getViewport().getViewPosition().y;
        int diff = scrollTarget - current;

        if (Math.abs(diff) <= SCROLL_STEP) {
            scrollPane.getViewport().setViewPosition(new java.awt.Point(0, scrollTarget));
            scrollTimer.stop();
        } else {
            int step = diff > 0 ? SCROLL_STEP : -SCROLL_STEP;
            scrollPane.getViewport().setViewPosition(new java.awt.Point(0, current + step));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(View.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}