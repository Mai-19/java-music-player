package view.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.PlaylistMouseListener;
import model.Model;
import view.Icons;
import view.View;

public class PlaylistPanel extends JPanel {

    private final Model model;
    private final View view;

    private CardLayout cardLayout;
    private JPanel listCard;
    private JPanel playlistListPanel;
    private String selectedPlaylist;
    private MusicPlayerButton backBtn;
    private JLabel playlistTitle;
    private JPanel songsWrapper;

    public PlaylistPanel(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
        setOpaque(false);

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        listCard = buildListCard();

        // build layered pane for songs card with floating back button
        songsWrapper = new JPanel(new BorderLayout());
        songsWrapper.setOpaque(false);

        // top bar
        JPanel songsTopBar = new JPanel(new BorderLayout());
        songsTopBar.setOpaque(false);
        songsTopBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 40));

        backBtn = new MusicPlayerButton(Icons.BACK, 40, 40, 50); 
        backBtn.setActionCommand("close playlist");
        backBtn.addActionListener(view.getButtonListener());

        playlistTitle = new JLabel("", JLabel.CENTER);
        playlistTitle.setFont(playlistTitle.getFont().deriveFont(Font.BOLD, 20f));

        songsTopBar.add(backBtn, BorderLayout.WEST);
        songsTopBar.add(playlistTitle, BorderLayout.CENTER);

        songsWrapper.add(songsTopBar, BorderLayout.NORTH);

        add(listCard, "list");
        add(songsWrapper, "songs");

        cardLayout.show(this, "list");
    }

    private JPanel buildListCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // header row with title and add button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, View.ACCENT));

        JLabel title = new JLabel("Playlists");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(View.TEXT);

        MusicPlayerButton addBtn = new MusicPlayerButton(Icons.ADD_DIRECTORY);
        addBtn.setActionCommand("create playlist");
        addBtn.addActionListener(view.getButtonListener());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // scrollable list of playlists
        playlistListPanel = new JPanel();
        playlistListPanel.setLayout(new BoxLayout(playlistListPanel, BoxLayout.Y_AXIS));
        playlistListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(playlistListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshPlaylists();
        return panel;
    }

    public void refreshPlaylists() {
        playlistListPanel.removeAll();
        List<String> playlists = model.loadPlaylists();
        for (String name : playlists) {
            playlistListPanel.add(buildPlaylistRow(name));
        }
        playlistListPanel.add(Box.createVerticalGlue());
        playlistListPanel.revalidate();
        playlistListPanel.repaint();
    }

    private JPanel buildPlaylistRow(String name) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 12));

        JLabel label = new JLabel(name);
        label.setForeground(View.TEXT);

        MusicPlayerButton deleteBtn = new MusicPlayerButton(Icons.TRASH);
        deleteBtn.setActionCommand("delete playlist:" + name);
        deleteBtn.addActionListener(view.getButtonListener());

        row.add(label, BorderLayout.WEST);
        row.add(deleteBtn, BorderLayout.EAST);

        // click row to open playlist
        row.addMouseListener(new PlaylistMouseListener(this, name));

        return row;
    }

    public void openPlaylist(String name) {
        selectedPlaylist = name;
        playlistTitle.setText(name);
        model.setSongs(model.loadSongsForPlaylist(name));

        if (view.getMusicPanel().getParent() != null) {
            view.getMusicPanel().getParent().remove(view.getMusicPanel());
        }
        songsWrapper.add(view.getMusicPanel(), BorderLayout.CENTER);

        view.pullSongs();
        cardLayout.show(this, "songs");
    }

    public void showList() {
        selectedPlaylist = null;
        view.reclaimMusicPanel();
        model.loadSongsFromDatabase();
        view.pullSongs();
        cardLayout.show(this, "list");
    }

    public String getSelectedPlaylist() {
        return selectedPlaylist;
    }
}