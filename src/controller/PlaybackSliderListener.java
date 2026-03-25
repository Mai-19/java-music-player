package controller;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Model;
import view.View;
import view.components.MusicPlayerSlider;

/**
 * PlaybackSliderListener handles user interaction with the playback progress slider
 * 
 * while the user is dragging the slider playback is paused and the position is updated
 * so the user can hear where they are scrubbing to
 * when the user releases the slider playback resumes from the new position
 */
public class PlaybackSliderListener implements ChangeListener {

    private final Model model;
    private final View view;

    /**
     * creates the listener with references to the model and view
     * 
     * @param model the application model
     * @param view  the application view
     */
    public PlaybackSliderListener(Model model, View view) {
        super();
        this.view = view;
        this.model = model;
    }

    /**
     * called whenever the slider value changes either by the user or by the timer
     * 
     * checks getValueIsAdjusting to tell the difference between a user drag
     * and a programmatic update from the playback timer
     * only responds to user input by checking the adjusting flag
     * 
     * @param e the change event from the slider
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        MusicPlayerSlider slider = (MusicPlayerSlider) e.getSource();

        if (slider.getValueIsAdjusting()) {
            // user is dragging - pause playback set the flag and seek to the dragged position
            model.pausePlayback();
            model.setUserAdjustingTime(true);
            model.setPlaybackTime(slider.getValue());
        } else {
            // user released the slider - clear the flag and resume playback
            model.setUserAdjustingTime(false);
            model.resumePlayback();
            view.setPlayback("pause");
        }
    }
}
