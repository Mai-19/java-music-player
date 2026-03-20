package controller;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Model;
import view.View;
import view.components.MusicPlayerSlider;

/**
 * Class for handling the volume slider
 */
public class VolumeSliderListener implements ChangeListener {

    private Model model;
    private View view;

    /**
     * Constructor for the VolumeSliderListener class
     * @param model
     */
    public VolumeSliderListener(Model model, View view) {
        super();

        this.model = model;
        this.view = view;
    }

    /**
     * sets volume on volume bar drag
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
