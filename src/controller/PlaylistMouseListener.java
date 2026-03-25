package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.components.PlaylistPanel;

/**
 * PlaylistMouseListener listens for clicks on a playlist row in the playlist list
 * 
 * each row in the playlist list gets its own instance of this listener
 * with the name of the playlist it represents baked in at construction time
 * clicking the row calls openPlaylist on the PlaylistPanel to show that playlists songs
 */
public class PlaylistMouseListener extends MouseAdapter {

    private final PlaylistPanel playlistPanel;

    // the name of the playlist this listener is attached to
    private final String name;

    /**
     * creates the listener for a specific playlist row
     * 
     * @param playlistPanel the panel that manages the playlist views
     * @param name          the name of the playlist this row represents
     */
    public PlaylistMouseListener(PlaylistPanel playlistPanel, String name) {
        super();
        this.playlistPanel = playlistPanel;
        this.name = name;
    }

    /**
     * opens the playlist when the row is clicked
     * 
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        playlistPanel.openPlaylist(name);
    }
}
