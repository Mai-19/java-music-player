package view.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.Model;
import view.View;

/**
 * Class for the player panel
 */
public class PlayerPanel extends JPanel {
    private final Model model;
    private final View view;

    /**
     * Constructor for the PlayerPanel class
     * 
     * @param model
     * @param view
     */
    public PlayerPanel(Model model, View view) {
        super();

        this.model = model;
        this.view = view;
        setBackground(View.FOREGROUND);
        createLayout();
    }

    private TopBarPanel topBar;

    private MusicPlayerTabbedPane tabbedPane;

    private PlaylistPanel playlistPanel;

    private BottomBarPanel bottomBar;

    private RoundedPanel rounded;
    /**
     * Method for creating the layout of the player panel
     */
    private void createLayout() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topBar = new TopBarPanel(view);

        tabbedPane = new MusicPlayerTabbedPane(view);

        rounded = new RoundedPanel(view.getMusicPanel(), 60);
        tabbedPane.addTab("All Songs", rounded);

        playlistPanel = new PlaylistPanel(model, view);
        RoundedPanel temp_rounded = new RoundedPanel(playlistPanel, 60);
        tabbedPane.addTab("Playlists", temp_rounded);

        bottomBar = new BottomBarPanel(model, view);

        this.add(topBar, BorderLayout.NORTH);
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);
    }

    // getters
    public TopBarPanel getTopBar() {
        return topBar;
    }

    public PlaylistPanel getPlaylistsPanel() {
        return playlistPanel;
    }

    public MusicPlayerTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public BottomBarPanel getBottomBar() {
        return bottomBar;
    }

    public void reclaimMusicPanel() {
        if (view.getMusicPanel().getParent() != null) {
            view.getMusicPanel().getParent().remove(view.getMusicPanel());
        }
        rounded.add(view.getMusicPanel()); 
    }

}
