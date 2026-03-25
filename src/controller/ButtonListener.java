package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.Model;
import view.View;

/**
 * ButtonListener handles all button click events in the application
 * 
 * it implements ActionListener and is registered on every button in the view
 * action commands are used to identify which button was clicked
 * 
 * some commands include extra data after a colon separator
 * for example "remove directory:/home/user/music" where the path follows the colon
 * the split on the first colon is used to separate the command from its argument
 */
public class ButtonListener implements ActionListener {

    private final Model model;
    private final View view;

    /**
     * creates the ButtonListener with references to the model and view
     * 
     * @param model the application model
     * @param view  the application view
     */
    public ButtonListener(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
    }

    /**
     * called when any registered button is clicked
     * 
     * splits the action command on the first colon to extract the command name
     * and an optional argument used by commands like "remove directory"
     * 
     * @param e the action event containing the action command string
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // split the action command into 2 on the first colon
        // the first half of this is the action command
        String action = e.getActionCommand().split(":", 2)[0];
        // the second half of this is an optional addition
        // its currently only used in "remove directory" to specify the path to remove
        switch (action) {
            // open settings menu
            case "settings":
                view.changeView(View.Cards.SETTINGS);
                break;

            // leave settings menu back to the player
            case "back":
                view.changeView(View.Cards.PLAYER);
                view.pullSongs();
                break;

            // reindex songs in all directories and refresh the table
            case "refresh":
                model.indexSongs();
                view.pullSongs();
                break;

            // toggle between play and pause
            case "toggle playback":
                model.togglePlayback();
                view.togglePlayback();
                break;

            // skip ahead 5 seconds
            case "forward":
                model.forwardSong();
                view.setProgress(view.getProgress() + 5);
                view.setPlayback("pause");
                break;

            // skip back 5 seconds
            case "rewind":
                model.rewindSong();
                view.setProgress(view.getProgress() - 5);
                view.setPlayback("pause");
                break;

            // skip to the next song in the queue
            case "next":
                model.nextSong();
                break;

            // go back to the previous song
            case "previous":
                model.previousSong();
                break;

            // shuffle or unshuffle the queue
            case "shuffle":
                model.shuffleSongs();
                view.pullSongs();
                break;

            // toggle repeat for the current song
            case "repeat":
                model.repeatSong();
                view.pullSongs();
                break;

            // toggle mute on the volume button
            case "toggle mute":
                view.toggleMute();
                break;

            // open the file chooser to add a new music directory
            // indexing runs on a background thread to keep the UI responsive
            case "add directory":
                if (view.addDirectory()) {
                    new Thread(() -> {
                        model.indexSongs();
                        SwingUtilities.invokeLater(() -> {
                            view.pullSongs();
                            view.refreshDirectoryList();
                        });
                    }).start();
                }
                break;

            // remove the directory whose path follows the colon in the action command
            case "remove directory":
                model.removeDirectory(e.getActionCommand().split(":", 2)[1]);
                view.refreshDirectoryList();
                view.pullSongs();
                break;

            // prompt the user for a name and create a new playlist
            case "create playlist":
                String name = JOptionPane.showInputDialog(view.getFrame(), "Playlist name:");
                if (name != null && !name.isBlank()) {
                    model.createPlaylist(name);
                    view.getPlayerPanel().getPlaylistsPanel().refreshPlaylists();
                }
                break;

            // ask for confirmation then delete the named playlist
            case "delete playlist":
                String playlistName = e.getActionCommand().split(":", 2)[1];
                int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                        "Delete playlist \"" + playlistName + "\"?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    model.deletePlaylist(playlistName);
                    view.getPlayerPanel().getPlaylistsPanel().refreshPlaylists();
                }
                break;

            // close the currently open playlist and go back to the playlist list
            case "close playlist":
                view.getPlayerPanel().getPlaylistsPanel().showList();
                break;

            // open a save dialog and export the weekly stats image
            case "download stats":
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File("weekly-stats.png"));
                int result = chooser.showSaveDialog(view.getFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        model.exportTopSongsImage(chooser.getSelectedFile().getAbsolutePath());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                break;

            // unused fallback
            default:
                break;
        }
    }
}
