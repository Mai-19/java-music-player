package view.components;

import controller.PopupMouseListener;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import java.awt.Font;
import java.awt.Point;
import java.util.List;
import model.Model;
import model.Song;
import view.View;

/**
 * MusicPlayerTablePopup is the right-click context menu for the song table
 *
 * it dynamically builds its items each time it is shown
 * offering options to add the selected songs to a playlist
 * or remove them from the currently open playlist
 */
public class MusicPlayerTablePopup extends JPopupMenu {

    private final Model model;
    private final View view;
    private final MusicPlayerTable table;

    public MusicPlayerTablePopup(MusicPlayerTable table, Model model, View view) {
        this.table = table;
        this.model = model;
        this.view = view;

        setBorder(new LineBorder(View.BACKGROUND, 0, true));
        setLightWeightPopupEnabled(false);

        table.addMouseListener(new PopupMouseListener(table, this));
    }

    public void buildMenu() {
        removeAll();

        JMenu addToPlaylist = makeMenu("Add to playlist");
        List<String> playlists = model.loadPlaylists();

        if (playlists.isEmpty()) {
            JMenuItem none = makeMenuItem("No playlists");
            none.setFont(none.getFont().deriveFont(Font.ITALIC, 14f));
            none.setEnabled(false);
            addToPlaylist.add(none);
        } else {
            for (String playlist : playlists) {
                JMenuItem item = makeMenuItem(playlist);
                item.addActionListener(e -> addSelectedSongsToPlaylist(playlist));
                addToPlaylist.add(item);
            }
        }
        add(addToPlaylist);

        MusicPlayerTabbedPane tabbedPane = view.getPlayerPanel().getTabbedPane();
        if (tabbedPane.getSelectedIndex() != 0) {
            addSeparator();
            JMenuItem removeItem = makeMenuItem("Remove from playlist");
            removeItem.addActionListener(e -> removeSelectedSongsFromPlaylist());
            add(removeItem);
        }
    }

    private JMenu makeMenu(String text) {
        JMenu menu = new JMenu(text) {
            @Override
            protected Point getPopupMenuOrigin() {
                Point p = super.getPopupMenuOrigin();
                p.x += 4; // shift right
                return p;
            }
        };
        menu.setOpaque(true);
        menu.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        menu.setFont(menu.getFont().deriveFont(14f));
        menu.getPopupMenu().setBorder(new LineBorder(View.BACKGROUND, 0, true));
        return menu;
    }

    private JMenuItem makeMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        item.setFont(item.getFont().deriveFont(14f));
        return item;
    }

    private void addSelectedSongsToPlaylist(String playlistName) {
        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Song song = model.getSongs().get(modelRow);
            model.addSongToPlaylist(playlistName, song.getPath());
        }
    }

    private void removeSelectedSongsFromPlaylist() {
        String playlist = view.getPlayerPanel().getPlaylistsPanel().getSelectedPlaylist();
        if (playlist == null) return;
        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Song song = model.getSongs().get(modelRow);
            model.removeSongFromPlaylist(playlist, song.getPath());
        }
        model.setSongs(model.loadSongsForPlaylist(playlist));
        view.pullSongs();
    }
}