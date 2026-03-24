package controller;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import model.Model;
import view.View;

/**
 * Class for handling the mouse clicks on the table
 */
public class TableMouseListener extends MouseAdapter{

    private final View view;
    private final Model model;

    /**
     * Constructor for the TableMouseListener class
     * @param view
     * @param model
     */
    public TableMouseListener(View view, Model model) {
        super();
        this.view = view;
        this.model = model;
    }

    /**
     * on click of a row, plays that song.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // get row index
            int row = view.rowAtPoint(e.getPoint());
            if (row != -1) {
                // play the song on that row
                // convert to index in model, because otherwise a search could return the wrong index
                model.play(view.convertRowIndexToModel(row));
                // set playback button to the pause button
                view.setPlayback("pause");
            }
        }
    }
}
