package controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.Model;
import view.Cards;
import view.View;

/**
 * ButtonListener class for all button interactions
 */
public class ButtonListener implements ActionListener{

    private Model model;
    private View view;
    
    /**
     * constructor for the ButtonListener class
     * @param model
     * @param view
     */
    public ButtonListener(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
    }

    /**
     * Override: <br>
     * actionPerformed method for the ButtonListener class
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // split the action command into 2 on the first colon.
        // the first half of this is the action command
        String action = e.getActionCommand().split(":", 2)[0];
        // the second half of this is an optional addition
        // its currently only used in "remove directory" to specify the path to remove
        switch (action) {
            // open settings menu
            case "settings":
                view.changeView(Cards.SETTINGS);
                break;
            // leave settings menu (to player)
            case "back":
                view.changeView(Cards.PLAYER);
                break;
            // reindex songs in the directories and get the view to pull it
            case "refresh":
                model.indexSongs();
                view.pullSongs();
                break;
            // play/pause the current song.
            case "toggle playback":
                model.togglePlayback();
                view.togglePlayback();
                break;
            // skip ahead 5 seconds
            case "forward":
                model.forwardSong();
                view.setProgress(view.getProgress()+5);
                view.setPlayback("pause");
                break;
            // skip backwards 5 seconds
            case "rewind":
                model.rewindSong();
                view.setProgress(view.getProgress()-5);
                view.setPlayback("pause");
                break;
            case "next":
                model.nextSong();
                break;
            case "previous":
                model.previousSong();
                break;
            // shuffle song queue
            case "shuffle":
                model.shuffleSongs();
                view.pullSongs();
                break;
            // repeat single song
            case "repeat":
                model.repeatSong();
                view.pullSongs();
                break;
            // mute the player
            case "toggle mute":
                view.toggleMute();
                break;
            // add directory to the list of directories to index
            case "add directory":
                view.addDirectory();
                model.indexSongs();
                view.pullSongs();
                break;
            // remove directory from the list of directories to index
            case "remove directory":
                model.removeDirectory(e.getActionCommand().split(":", 2)[1]);
                view.refreshDirectoryList();
                view.pullSongs();
                break;
            
            // unused
            default: break;
        }
    }
}
