package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.components.PlaylistPanel;

/**
 * Controller for clicking a playlist row to open it
 */
public class PlaylistMouseListener extends MouseAdapter {

    private final PlaylistPanel playlistPanel;
    private final String name;

    public PlaylistMouseListener(PlaylistPanel playlistPanel, String name) {
        super();
        this.playlistPanel = playlistPanel;
        this.name = name;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        playlistPanel.openPlaylist(name);
    }
}
