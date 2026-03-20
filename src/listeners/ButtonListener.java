package listeners;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.Model;
import view.Cards;
import view.View;

public class ButtonListener implements ActionListener{

    private Model model;
    private View view;
    
    public ButtonListener(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand().split(":", 2)[0]) {
            case "settings":
                view.changeView(Cards.SETTINGS);
                break;
            case "back":
                view.changeView(Cards.PLAYER);
                break;
            case "refresh":
                model.indexSongs();
                view.pullSongs();
                break;

            case "toggle playback":
                model.togglePlayback();
                view.togglePlayback();
                break;
            case "forward":
                model.forwardSong();
                break;
            case "rewind":
                model.rewindSong();
                break;
            case "toggle mute":
                view.toggleMute();
                break;
            case "add directory":
                view.addDirectory();
                break;
            case "remove directory":
                model.removeDirectory(e.getActionCommand().split(":", 2)[1]);
                view.refreshDirectoryList();
                break;

            default:
                break;
        }
    }
}
