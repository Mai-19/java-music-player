package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import model.Model;
import view.View;

/**
 * TableMouseListener handles mouse clicks on the song table
 * 
 * a double click on any row starts playing the song on that row
 * playback is started on a background thread to avoid freezing the UI
 * while the audio engine loads the file
 * 
 * the row index is converted from the view index to the model index
 * before playing because the table may be filtered or sorted
 * and the view row number would point to the wrong song
 */
public class TableMouseListener extends MouseAdapter {

    private final View view;
    private final Model model;

    /**
     * creates the listener with references to the view and model
     * 
     * @param view  the application view
     * @param model the application model
     */
    public TableMouseListener(View view, Model model) {
        super();
        this.view = view;
        this.model = model;
    }

    /**
     * called on any mouse click on the table
     * checks for a double click and plays the song on the clicked row
     * 
     * @param e the mouse event containing the click count and cursor position
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // get the row index in the view (may differ from model if filtered)
            int row = view.rowAtPoint(e.getPoint());
            if (row != -1) {
                // play the song - run on a background thread so the UI stays responsive
                new Thread(() -> {
                    // convert to model index so filtering does not pick the wrong song
                    model.play(view.convertRowIndexToModel(row));
                    // update the play button icon on the EDT after playback starts
                    SwingUtilities.invokeLater(() -> {
                        view.pullMetadata();
                        view.setPlayback("pause");
                    });
                }).start();
            }
        }
    }
}
