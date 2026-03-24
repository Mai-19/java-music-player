package controller;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Model;
import view.View;

/**
 * PlaybackTimer class for handling the playback of music.
 */
public class PlaybackTimer implements ActionListener {
    private final Model model;
    private final View view;
    private Timer timer;

    /**
     * Constructor for the PlaybackTimer class.
     * @param model
     * @param view
     */
    public PlaybackTimer(Model model, View view) {
        this.model = model;
        this.view = view;
        timer = new Timer(250, this);
    }

    /**
     * start timer
     */
    public void start() {
        timer.start();
    }

    /**
     * end timer
     */
    public void stop() {
        timer.stop();
    }

    /**
     * every time the timer ticks, this method is called <br>
     * the method updates the playback bars progress <br>
     * and pulls metadata from the model for the current playing song
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // get metadata on change
        if (model.hasMetadataChanged()) view.pullMetadata();
        // update progress bar
        if (!model.isAdjustingTime()) {
            int progress = model.getProgress();
            if (progress != -1) view.setProgress(progress);
        }
    }
}