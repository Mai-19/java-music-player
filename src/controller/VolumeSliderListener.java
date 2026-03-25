package controller;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Model;
import view.View;
import view.components.MusicPlayerSlider;

/**
 * VolumeSliderListener handles user interaction with the volume slider
 * 
 * updates the model volume in real time while the slider is being dragged
 * also updates the volume button icon in the view so it switches to the
 * muted icon when the slider reaches zero
 */
public class VolumeSliderListener implements ChangeListener {

    private final Model model;
    private final View view;

    /**
     * creates the listener with references to the model and view
     * 
     * @param model the application model
     * @param view  the application view
     */
    public VolumeSliderListener(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
    }

    /**
     * called whenever the volume slider value changes
     * only updates volume while the user is actively dragging
     * to avoid responding to programmatic value changes
     * 
     * @param e the change event from the slider
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        MusicPlayerSlider slider = (MusicPlayerSlider) e.getSource();

        if (slider.getValueIsAdjusting()) {
            model.setVolume(slider.getValue());
            view.setVolume(slider.getValue());
        }
    }
}
