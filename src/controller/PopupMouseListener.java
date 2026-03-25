package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.components.MusicPlayerTable;
import view.components.MusicPlayerTablePopup;

/**
 * PopupMouseListener triggers the right-click context menu on the song table
 * 
 * it listens on both mousePressed and mouseReleased to handle the difference
 * between how right-click popup triggers work on different operating systems
 * on Windows the trigger fires on release - on Mac it fires on press
 * 
 * before showing the popup it selects the row under the cursor if it is not already selected
 */
public class PopupMouseListener extends MouseAdapter {

    private final MusicPlayerTable table;
    private final MusicPlayerTablePopup popup;

    /**
     * creates the listener for the given table and popup menu
     * 
     * @param table the song table to listen on
     * @param popup the popup menu to show on right click
     */
    public PopupMouseListener(MusicPlayerTable table, MusicPlayerTablePopup popup) {
        super();
        this.table = table;
        this.popup = popup;
    }

    /**
     * handles a mouse press - checks if it is a popup trigger
     * 
     * @param e the mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        handleClick(e);
    }

    /**
     * handles a mouse release - checks if it is a popup trigger
     * 
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        handleClick(e);
    }

    /**
     * shows the popup menu if the event is a right-click popup trigger
     * selects the row under the cursor first so the menu operates on the right song
     * 
     * @param e the mouse event to check
     */
    private void handleClick(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0 && !table.isRowSelected(row))
            table.setRowSelectionInterval(row, row);
        popup.buildMenu();
        popup.show(table, e.getX(), e.getY());
    }
}
