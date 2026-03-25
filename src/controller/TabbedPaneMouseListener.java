package controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import view.components.MusicPlayerTabbedPane;

/**
 * TabbedPaneMouseListener handles hover effects on the custom tabbed pane
 * 
 * tracks which tab the mouse is currently over so the custom tab renderer
 * in MusicPlayerTabbedPane can paint a hover highlight on the right tab
 * when the mouse leaves the tab area entirely the hover state is cleared
 */
public class TabbedPaneMouseListener implements MouseListener, MouseMotionListener {

    /**
     * creates the listener
     */
    public TabbedPaneMouseListener() {
        super();
    }

    /** not used */
    @Override
    public void mouseDragged(MouseEvent e) {}

    /**
     * called when the mouse moves over the tabbed pane
     * finds which tab is under the cursor and updates the hovered tab index
     * triggers a repaint so the hover highlight updates immediately
     * 
     * @param e the mouse event containing the cursor position
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        MusicPlayerTabbedPane mp = (MusicPlayerTabbedPane) e.getSource();
        int tab = mp.getUI().tabForCoordinate(mp, e.getX(), e.getY());
        if (tab != mp.getHoveredTab()) {
            mp.setHoveredTab(tab);
        }
        mp.repaint();
    }

    /** not used */
    @Override public void mouseClicked(MouseEvent e) {}

    /** not used */
    @Override public void mouseEntered(MouseEvent e) {}

    /**
     * called when the mouse leaves the tabbed pane entirely
     * clears the hover state so no tab appears highlighted
     * 
     * @param e the mouse event
     */
    @Override
    public void mouseExited(MouseEvent e) {
        MusicPlayerTabbedPane mp = (MusicPlayerTabbedPane) e.getSource();
        mp.setHoveredTab(-1);
        mp.repaint();
    }

    /** not used */
    @Override public void mousePressed(MouseEvent e) {}

    /** not used */
    @Override public void mouseReleased(MouseEvent e) {}
}
