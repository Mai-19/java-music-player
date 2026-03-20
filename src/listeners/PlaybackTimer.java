package listeners;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Model;
import view.View;

public class PlaybackTimer implements ActionListener {
    private Model model;
    private View view;
    private Timer timer;

    public PlaybackTimer(Model model, View view) {
        this.model = model;
        this.view = view;
        timer = new Timer(250, this);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (model.hasMetadataChanged()) view.pullMetadata();
        if (!model.isAdjustingTime()) {
            int progress = model.getProgress();
            if (progress != -1) view.setProgress(progress);
        }
    }
}