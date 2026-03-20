package controller;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Model;
import view.View;
import view.components.MusicPlayerSlider;

/**
 * Listener for the playback slider  <br>
 * pauses playback while user drags playback slider
 */
public class PlaybackSliderListener implements ChangeListener {

    private Model model;
    private View view;

    /**
     * Constructor for the PlaybackSliderListener class
     * @param model
     */
    public PlaybackSliderListener(Model model, View view) {
        super();

        this.view = view;
        this.model = model;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        MusicPlayerSlider slider = (MusicPlayerSlider) e.getSource();

        // if being dragged
        if (slider.getValueIsAdjusting()) {
            // pause playback, set flag, and set playback time for when the user lets go
            model.pausePlayback();
            model.setUserAdjustingTime(true);
            model.setPlaybackTime(slider.getValue());
        }
        else {
            // continue playback and unset flag
            model.setUserAdjustingTime(false);
            model.resumePlayback();
            view.setPlayback("pause");
            
        }
    }
    
}
