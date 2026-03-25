package view;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.UIManager;

import controller.ButtonListener;
import controller.PlaybackTimer;
import controller.ResizeListener;
import model.Model;
import view.components.MusicPanel;
import view.components.PlayerPanel;
import view.components.SettingsPanel;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.util.List;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.DisplayMode;

/**
 * View is the top-level UI class for the MusicPlayer application
 * 
 * it follows the View layer of the MVC pattern - it builds and owns the JFrame
 * and all panels and delegates user actions to the controllers
 * 
 * the view uses a CardLayout to switch between the main player screen
 * and the settings screen without creating or destroying components
 * 
 * global theme colors are stored as public static fields so every
 * component can access them without needing a reference to View
 */
public class View {

    // size in pixels for the square album art image in the bottom bar
    public static final int ALBUM_IMG_SIZE;

    // global theme colors used by all components
    public static Color BACKGROUND;
    public static Color FOREGROUND;
    public static Color ACCENT;
    public static Color TEXT;

    private final Model model;

    // information about the current display including refresh rate and resolution
    private DisplayMode screen;

    private ResizeListener resizeListener;
    private ButtonListener buttonListener;

    // the shared music panel that is swapped between the all songs view and playlist views
    private MusicPanel musicPanel;

    /**
     * creates the View and builds the entire UI
     * 
     * sets global UIManager properties to theme all Swing dialogs
     * starts the playback timer that drives progress bar updates
     * then creates the JFrame and registers all controllers
     * 
     * @param model the application model
     */
    public View(Model model) {
        super();

        this.model = model;

        // theme the popup menu and menu items
        UIManager.put("PopupMenu.background",           View.BACKGROUND.brighter().brighter());
        UIManager.put("MenuItem.background",            View.BACKGROUND.brighter().brighter());
        UIManager.put("MenuItem.foreground",            View.TEXT);
        UIManager.put("MenuItem.selectionBackground",   View.ACCENT.brighter());
        UIManager.put("MenuItem.selectionForeground",   View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.background",                View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.foreground",                View.TEXT);
        UIManager.put("Menu.selectionBackground",       View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.selectionForeground",       View.TEXT);

        // theme the file chooser and common dialog components
        UIManager.put("FileChooser.background",  View.FOREGROUND);
        UIManager.put("FileChooser.foreground",  View.TEXT);
        UIManager.put("Panel.background",        View.FOREGROUND);
        UIManager.put("Label.foreground",        View.TEXT);
        UIManager.put("Button.background",       View.BACKGROUND);
        UIManager.put("Button.foreground",       View.TEXT);
        UIManager.put("TextField.background",    View.BACKGROUND);
        UIManager.put("TextField.foreground",    View.TEXT);
        UIManager.put("List.background",         View.BACKGROUND);
        UIManager.put("List.foreground",         View.TEXT);
        UIManager.put("List.selectionBackground",View.ACCENT);
        UIManager.put("List.selectionForeground",View.TEXT);

        // file chooser specific overrides
        UIManager.put("FileChooser.listBackground",   View.FOREGROUND);
        UIManager.put("FileChooser.listForeground",   View.TEXT);

        // option pane specific overrides
        UIManager.put("OptionPane.background",          View.FOREGROUND);
        UIManager.put("OptionPane.foreground",          View.TEXT);
        UIManager.put("OptionPane.messageForeground",   View.TEXT);

        // text field overrides
        UIManager.put("TextField.caretForeground", View.TEXT);

        // combo box overrides for the file chooser look-in dropdown
        UIManager.put("ComboBox.background",          View.BACKGROUND);
        UIManager.put("ComboBox.foreground",          View.TEXT);
        UIManager.put("ComboBox.selectionBackground", View.ACCENT);
        UIManager.put("ComboBox.selectionForeground", View.TEXT);
        UIManager.put("TextField.border", BorderFactory.createEmptyBorder(4, 4, 4, 4));
        UIManager.put("ComboBox.border",  BorderFactory.createEmptyBorder());

        PlaybackTimer playbackTimer = new PlaybackTimer(model, this);
        playbackTimer.start();

        // the music panel is shared between the all songs tab and playlist views
        musicPanel = new MusicPanel(model, this);

        resizeListener  = new ResizeListener(this);
        buttonListener  = new ButtonListener(model, this);

        createFrame();
        registerControllers();
        pullSongs();
        update();
    }

    // the main application window
    private JFrame frame;

    /**
     * creates the JFrame window at half the screen size
     * sets the title icon images at multiple sizes for the OS taskbar
     */
    private void createFrame() {
        screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        frame = new JFrame();
        frame.setFont(UIManager.getFont("Label.font"));
        frame.setTitle("MusicPlayer");
        frame.setIconImages(List.of(
            Icons.MUSIC_16.getImage(),
            Icons.MUSIC_32.getImage(),
            Icons.MUSIC_64.getImage(),
            Icons.MUSIC_256.getImage()));

        addComponents();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // half the screen in each dimension
        frame.setSize(screen.getWidth() / 2, screen.getHeight() / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // the root panel that holds all screens via CardLayout
    private JPanel contentPane;
    private CardLayout cardLayout;

    // the two main screens
    private PlayerPanel playerPanel;
    private SettingsPanel settingsPanel;

    /**
     * creates the CardLayout content pane and adds the player and settings panels to it
     * the player panel is shown by default
     */
    private void addComponents() {
        cardLayout  = new CardLayout();
        contentPane = new JPanel(cardLayout);

        playerPanel   = new PlayerPanel(model, this);
        settingsPanel = new SettingsPanel(model, this);

        contentPane.add(playerPanel,   Cards.PLAYER.name());
        contentPane.add(settingsPanel, Cards.SETTINGS.name());

        frame.setContentPane(contentPane);
    }

    /**
     * registers all controller listeners on the relevant UI components
     * called once during construction after all panels have been created
     */
    private void registerControllers() {
        frame.addComponentListener(resizeListener);
        playerPanel.getTopBar().getSettingsButton().addActionListener(buttonListener);
        playerPanel.getBottomBar().addActionListener(buttonListener);
        settingsPanel.getBackBtn().addActionListener(buttonListener);
        settingsPanel.getRefreshBtn().addActionListener(buttonListener);
        settingsPanel.getAddDirectoryBtn().addActionListener(buttonListener);
        settingsPanel.getDownloadStatsBtn().addActionListener(buttonListener);
    }

    /**
     * switches the visible panel using the CardLayout
     * 
     * @param card the screen to switch to - either PLAYER or SETTINGS
     */
    public void changeView(Cards card) {
        switch (card) {
            case SETTINGS:
                cardLayout.show(contentPane, Cards.SETTINGS.name());
                break;
            case PLAYER:
                cardLayout.show(contentPane, Cards.PLAYER.name());
                break;
            default:
                break;
        }
    }

    /**
     * triggers a repaint of the entire window
     * called on resize and after layout changes
     */
    public void update() {
        frame.repaint();
    }

    /**
     * updates the playback slider position and the current time label
     * also updates the lyrics panel scroll position if lyrics are showing
     * 
     * @param value current playback position in seconds
     */
    public void setProgress(int value) {
        playerPanel.getBottomBar().setProgress(value);
        playerPanel.getBottomBar().setCurrentTime(
            (value / 60) + ":" + ((value % 60) < 10 ? "0" + (value % 60) : (value % 60)));
        if (playerPanel.getLyricsPanel() != null)
            playerPanel.getLyricsPanel().updatePosition(value);
    }

    /** toggles the mute state on the volume button in the bottom bar */
    public void toggleMute() {
        playerPanel.getBottomBar().toggleMute();
    }

    /**
     * updates the volume button icon based on the current volume value
     * 
     * @param val the current volume slider value
     */
    public void setVolume(float val) {
        playerPanel.getBottomBar().setVolume(val);
    }

    /** toggles the play/pause button icon in the bottom bar */
    public void togglePlayback() {
        playerPanel.getBottomBar().togglePlayback();
    }

    /**
     * opens the directory file chooser dialog
     * 
     * @return true if the user selected a directory
     */
    public boolean addDirectory() {
        return settingsPanel.openFileChooser();
    }

    /** refreshes the directory list shown in the settings panel */
    public void refreshDirectoryList() {
        settingsPanel.refreshDirectoryList();
    }

    /**
     * pushes the current song queue from the model into the song table
     * called after any operation that changes the queue
     */
    public void pullSongs() {
        musicPanel.setData(model.getQueue());
    }

    /**
     * reads the current songs metadata from the model and updates all bottom bar labels
     * also updates the lyrics panel and highlights the playing row in the table
     * called by the playback timer when metadataChanged is true
     */
    public void pullMetadata() {
        playerPanel.getBottomBar().setSongTitle(model.getTitle());
        playerPanel.getBottomBar().setAlbum(model.getAlbum());
        playerPanel.getBottomBar().setAlbumArt(
            new ImageIcon(new ImageIcon(model.getArtworkBytes())
                .getImage().getScaledInstance(ALBUM_IMG_SIZE, ALBUM_IMG_SIZE, Image.SCALE_SMOOTH)));
        playerPanel.getBottomBar().setTotalTime(model.getLength(), model.getSeconds());
        playerPanel.getLyricsPanel().setLyrics(model.parseLrc(model.getSongs().get(model.getIndex()).getPath()));
        musicPanel.setRowSelection(model.getIndex());
        model.markMetadataRetrieved();
    }

    /**
     * resets all bottom bar labels and artwork to their empty defaults
     * called when no song is currently playing
     */
    public void clearMetadata() {
        if (playerPanel == null) return;
        playerPanel.getBottomBar().setSongTitle("");
        playerPanel.getBottomBar().setAlbum("");
        playerPanel.getBottomBar().setCurrentTime("0:00");
        playerPanel.getBottomBar().setAlbumArt(Icons.PLACEHOLDER_ALBUM);
        playerPanel.getBottomBar().setTotalTime("0:00", 0);
    }

    /**
     * returns the current progress slider value in seconds
     * 
     * @return current playback position in seconds
     */
    public int getProgress() {
        return playerPanel.getBottomBar().getProgress();
    }

    /** @return the shared ButtonListener registered on all buttons */
    public ButtonListener getButtonListener() {
        return buttonListener;
    }

    /**
     * returns the row index in the song table at the given screen point
     * used by TableMouseListener to find which row was clicked
     * 
     * @param point the screen coordinates to check
     * @return the row index or -1 if no row is at that point
     */
    public int rowAtPoint(Point point) {
        return musicPanel.rowAtPoint(point);
    }

    /**
     * converts a view row index to the corresponding model row index
     * necessary when the table is filtered because the row numbers differ
     * 
     * @param row the view row index
     * @return the corresponding model row index
     */
    public int convertRowIndexToModel(int row) {
        return musicPanel.convertRowIndexToModel(row);
    }

    /**
     * applies a row filter to the song table
     * pass null to remove the filter and show all songs
     * 
     * @param regexFilter the filter to apply or null to clear
     */
    public void setRowFilter(RowFilter<Object, Object> regexFilter) {
        musicPanel.getTableSorter().setRowFilter(regexFilter);
    }

    /**
     * sets the play/pause button icon
     * 
     * @param string "play" to show the play icon or "pause" to show the pause icon
     */
    public void setPlayback(String string) {
        playerPanel.getBottomBar().setPlayback(string);
    }

    // static initializer sets the album image size constant
    static {
        ALBUM_IMG_SIZE = 80;
    }

    /** @return the main application JFrame */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * enum representing the two main screens managed by the CardLayout
     */
    public enum Cards {
        PLAYER,
        SETTINGS
    }

    /** @return the player panel containing the tabs and bottom bar */
    public PlayerPanel getPlayerPanel() {
        return playerPanel;
    }

    /**
     * returns the component currently selected in the tabbed pane
     * used to check which tab is active
     * 
     * @return the selected tab component
     */
    public Component getSelectedPlaylist() {
        return playerPanel.getTabbedPane().getSelectedComponent();
    }

    /**
     * moves the shared music panel back into the all songs tab
     * called when closing a playlist view so the panel is available for the main view
     */
    public void reclaimMusicPanel() {
        playerPanel.reclaimMusicPanel();
    }

    /** @return the shared music panel containing the song table */
    public MusicPanel getMusicPanel() {
        return musicPanel;
    }

    /** @return true if the main frame is currently visible */
    public boolean isVisible() {
        return frame.isVisible();
    }

    // static initializer sets the global theme colors
    static {
        BACKGROUND = new Color(33, 32, 32);
        FOREGROUND = BACKGROUND.darker();
        ACCENT     = new Color(82, 78, 78);
        TEXT       = new Color(200, 200, 200);
    }
}
