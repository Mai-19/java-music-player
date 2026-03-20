package view.components;
import javax.swing.*;

public class MusicPlayerSlider extends JSlider {
    public MusicPlayerSlider(int min, int max, int value) {
        super(min, max, value);
        setOpaque(false);
        setUI(new MusicPlayerSliderUI(this));
    }
}