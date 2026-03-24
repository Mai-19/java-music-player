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
 * class with all the view components
 */
public class View {
    public static final int ALBUM_IMG_SIZE;

    public static Color BACKGROUND;
    public static Color FOREGROUND;
    public static Color ACCENT;
    public static Color TEXT;

    private final Model model;
    // the screens information, like refresh rate and size
    private DisplayMode screen;

    private ResizeListener resizeListener;
    private ButtonListener buttonListener;

    private MusicPanel musicPanel;

    /**
     * constructor for the view class
     * @param model
     */
    public View(Model model) {
        super();

        this.model = model;

        UIManager.put("PopupMenu.background", View.BACKGROUND.brighter().brighter());
        UIManager.put("MenuItem.background", View.BACKGROUND.brighter().brighter());
        UIManager.put("MenuItem.foreground", View.TEXT);
        UIManager.put("MenuItem.selectionBackground", View.ACCENT.brighter());
        UIManager.put("MenuItem.selectionForeground", View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.background", View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.foreground", View.TEXT);
        UIManager.put("Menu.selectionBackground", View.BACKGROUND.brighter().brighter());
        UIManager.put("Menu.selectionForeground", View.TEXT);
        // more global
        UIManager.put("FileChooser.background", View.FOREGROUND);
        UIManager.put("FileChooser.foreground", View.TEXT);
        UIManager.put("Panel.background", View.FOREGROUND);
        UIManager.put("Label.foreground", View.TEXT);
        UIManager.put("Button.background", View.BACKGROUND);
        UIManager.put("Button.foreground", View.TEXT);
        UIManager.put("TextField.background", View.BACKGROUND);
        UIManager.put("TextField.foreground", View.TEXT);
        UIManager.put("List.background", View.BACKGROUND);
        UIManager.put("List.foreground", View.TEXT);
        UIManager.put("List.selectionBackground", View.ACCENT);
        UIManager.put("List.selectionForeground", View.TEXT);
        // File chooser specific
        UIManager.put("FileChooser.background", View.FOREGROUND);
        UIManager.put("FileChooser.foreground", View.TEXT);
        UIManager.put("FileChooser.listBackground", View.FOREGROUND);
        UIManager.put("FileChooser.listForeground", View.TEXT);
        UIManager.put("FileChooser.lookInLabelText", "Look In:");
        UIManager.put("FileView.directoryIcon", null);
        // Option pane specific  
        UIManager.put("OptionPane.background", View.FOREGROUND);
        UIManager.put("OptionPane.foreground", View.TEXT);
        UIManager.put("OptionPane.messageForeground", View.TEXT);
        // Text fields
        UIManager.put("TextField.background", View.BACKGROUND);
        UIManager.put("TextField.foreground", View.TEXT);
        UIManager.put("TextField.caretForeground", View.TEXT);
        // Lists (file chooser file list)
        UIManager.put("List.background", View.FOREGROUND);
        UIManager.put("List.foreground", View.TEXT);
        UIManager.put("List.selectionBackground", View.ACCENT);
        UIManager.put("List.selectionForeground", View.TEXT);
        // Combo boxes (Look In dropdown)
        UIManager.put("ComboBox.background", View.BACKGROUND);
        UIManager.put("ComboBox.foreground", View.TEXT);
        UIManager.put("ComboBox.selectionBackground", View.ACCENT);
        UIManager.put("ComboBox.selectionForeground", View.TEXT);
        UIManager.put("TextField.border", BorderFactory.createEmptyBorder(4, 4, 4, 4));
        UIManager.put("ComboBox.border", BorderFactory.createEmptyBorder());

        PlaybackTimer playbackTimer = new PlaybackTimer(model, this);
        playbackTimer.start();

        musicPanel = new MusicPanel(model, this);

        resizeListener = new ResizeListener(this);
        buttonListener = new ButtonListener(model, this);
        
        createFrame();
        registerControllers();
        pullSongs();
        update();
    }

    private JFrame frame;
    /**
     * creates a new JFrame with the model as its controller
     */
    private void createFrame() {
        // getting the default screen
        screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        frame = new JFrame();
        frame.setFont(UIManager.getFont("Label.font"));
        frame.setTitle("MusicPlayer");
        frame.setIconImages(List.of(Icons.MUSIC_16.getImage(), Icons.MUSIC_32.getImage(), Icons.MUSIC_64.getImage(), Icons.MUSIC_256.getImage()));

        addComponents();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // make the window half the size of the screen in each dimension
        frame.setSize(screen.getWidth()/2, screen.getHeight()/2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // the main panel of the view
    // will handle swapping between the main view and settings view with a card layout
    private JPanel contentPane;
    private CardLayout cardLayout;

    // the different panel views
    private PlayerPanel playerPanel;
    private SettingsPanel settingsPanel;

    /**
     * add the components to the view
     */
    private void addComponents() {
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);

        playerPanel = new PlayerPanel(model, this);
        settingsPanel = new SettingsPanel(model, this);

        contentPane.add(playerPanel, Cards.PLAYER.name());
        contentPane.add(settingsPanel, Cards.SETTINGS.name());
        
        frame.setContentPane(contentPane);
    }
    
    /**
     * register the controllers for the view
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
     * change the view of the application
     * @param settings
     */
    public void changeView(Cards settings) {
        switch (settings) {
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
     * updates view
     */
    public void update() {
        frame.repaint();
    }

    /**
     * sets progress on the playback slider
     * @param value
     */
    public void setProgress(int value) {
        playerPanel.getBottomBar().setProgress(value);
        playerPanel.getBottomBar().setCurrentTime(
            (value/60)
            +":"+
            ((value%60)<10?"0"+(value%60) : (value%60)));
    }

    // interactions
    public void toggleMute() {
        playerPanel.getBottomBar().toggleMute();
    }
    public void setVolume(float val) {
        playerPanel.getBottomBar().setVolume(val);
    }
    public void togglePlayback() {
        playerPanel.getBottomBar().togglePlayback();
    }
    public boolean addDirectory() {
        return settingsPanel.openFileChooser();
    }
    public void refreshDirectoryList() {
        settingsPanel.refreshDirectoryList();
    }
    public void pullSongs() {
        musicPanel.setData(model.getQueue());
    }
    public void pullMetadata() {
        playerPanel.getBottomBar().setSongTitle(model.getTitle());
        playerPanel.getBottomBar().setAlbum(model.getAlbum());
        playerPanel.getBottomBar().setAlbumArt(new ImageIcon(new ImageIcon(model.getArtworkBytes()).getImage().getScaledInstance(ALBUM_IMG_SIZE, ALBUM_IMG_SIZE, Image.SCALE_SMOOTH)));
        playerPanel.getBottomBar().setTotalTime(model.getLength(), model.getSeconds());
        musicPanel.setRowSelection(model.getIndex());
    }
    public void clearMetadata() {
        if (playerPanel == null) return;
        playerPanel.getBottomBar().setSongTitle("");
        playerPanel.getBottomBar().setAlbum("");
        playerPanel.getBottomBar().setCurrentTime("0:00");
        playerPanel.getBottomBar().setAlbumArt(Icons.PLACEHOLDER_ALBUM);
        playerPanel.getBottomBar().setTotalTime("0:00", 0);
    }

    // getters
    public int getProgress() {
        return playerPanel.getBottomBar().getProgress();
    }
    public ButtonListener getButtonListener() {
        return buttonListener;
    }
    public int rowAtPoint(Point point) {
        return musicPanel.rowAtPoint(point);
    }
    public int convertRowIndexToModel(int row) {
        return musicPanel.convertRowIndexToModel(row);
    }

    // setters
    public void setRowFilter(RowFilter<Object,Object> regexFilter) {
        musicPanel.getTableSorter().setRowFilter(regexFilter);
    }
    public void setPlayback(String string) {
        playerPanel.getBottomBar().setPlayback(string);
    }

    // static
    static {
        ALBUM_IMG_SIZE = 80;
    }

    public JFrame getFrame() {
        return frame;
    }

    public enum Cards {
        PLAYER,
        SETTINGS
    }

    public PlayerPanel getPlayerPanel() {
        return playerPanel;
    }

    public Component getSelectedPlaylist() {
        return playerPanel.getTabbedPane().getSelectedComponent();
    }

    public void reclaimMusicPanel() {
        playerPanel.reclaimMusicPanel();
    }

    public MusicPanel getMusicPanel() {
        return musicPanel;
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    static {
        BACKGROUND = new Color(33, 32, 32);
        FOREGROUND = BACKGROUND.darker();
        ACCENT = new Color(82, 78, 78);
        TEXT = new Color(200, 200, 200);
    }
}
