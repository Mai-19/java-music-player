package controller;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Model;
import view.View;

/**
 * PlaybackTimer drives the UI updates that need to happen continuously during playback
 * 
 * it fires every 250 milliseconds on the Swing event dispatch thread
 * on each tick it checks if the song metadata has changed and updates the progress bar
 * using a Swing Timer ensures all UI updates happen safely on the EDT
 */
public class PlaybackTimer implements ActionListener {

    private final Model model;
    private final View view;

    // the underlying Swing timer that fires every 250ms
    private Timer timer;

    /**
     * creates the PlaybackTimer and sets up the Swing timer
     * the timer does not start automatically - call start() to begin
     * 
     * @param model the application model
     * @param view  the application view
     */
    public PlaybackTimer(Model model, View view) {
        this.model = model;
        this.view = view;
        timer = new Timer(250, this);
    }

    /**
     * starts the timer so it begins firing every 250ms
     */
    public void start() {
        timer.start();
    }

    /**
     * stops the timer
     */
    public void stop() {
        timer.stop();
    }

    /**
     * called every 250ms by the Swing timer
     * 
     * pulls fresh metadata from the model if a new song has started
     * updates the playback progress bar unless the user is currently dragging it
     * 
     * @param e the action event from the timer - not used directly
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // get metadata on change
        if (model.hasMetadataChanged())
            view.pullMetadata();

        // update progress bar - skip if user is dragging to avoid fighting their input
        if (!model.isAdjustingTime()) {
            int progress = model.getProgress();
            if (progress != -1) view.setProgress(progress);
        }
    }
}
