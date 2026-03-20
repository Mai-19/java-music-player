package listeners;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Model;
import view.components.MusicPlayerSlider;

public class PlaybackSliderListener implements ChangeListener {

    private Model model;

    public PlaybackSliderListener(Model model) {
        super();

        this.model = model;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        MusicPlayerSlider slider = (MusicPlayerSlider) e.getSource();

        if (slider.getValueIsAdjusting()) {
            model.pausePlayback();
            model.setUserAdjustingTime(true);
            model.setPlaybackTime(slider.getValue());
        }
        else {
            model.setUserAdjustingTime(false);
            model.resumePlayback();
        }
    }
    
}
