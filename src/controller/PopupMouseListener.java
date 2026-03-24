package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.components.MusicPlayerTable;
import view.components.MusicPlayerTablePopup;

/**
 * Controller for right-click popup trigger on the music table
 */
public class PopupMouseListener extends MouseAdapter {

    private final MusicPlayerTable table;
    private final MusicPlayerTablePopup popup;

    public PopupMouseListener(MusicPlayerTable table, MusicPlayerTablePopup popup) {
        super();
        this.table = table;
        this.popup = popup;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        handleClick(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleClick(e);
    }

    private void handleClick(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0 && !table.isRowSelected(row))
            table.setRowSelectionInterval(row, row);
        popup.buildMenu();
        popup.show(table, e.getX(), e.getY());
    }
}
