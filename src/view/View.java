package view;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.UIManager;

import controller.ButtonListener;
import controller.PlaybackTimer;
import controller.ResizeListener;
import model.Model;
import view.components.PlayerPanel;
import view.components.SettingsPanel;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.CardLayout;
import java.awt.DisplayMode;

/**
 * class with all the view components
 */
public class View {
    public static final int ALBUM_IMG_SIZE;

    private Model model;
    // the screens information, like refresh rate and size
    private DisplayMode screen;

    private ResizeListener resizeListener;
    private ButtonListener buttonListener;
    /**
     * constructor for the view class
     * @param model
     */
    public View(Model model) {
        super();

        this.model = model;

        PlaybackTimer playbackTimer = new PlaybackTimer(model, this);
        playbackTimer.start();

        
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
    }

    /**
     * change the view of the application
     * @param settings
     */
    public void changeView(Cards settings) {
        switch (settings) {
            case Cards.SETTINGS:
                cardLayout.show(contentPane, Cards.SETTINGS.name());
                break;
            case Cards.PLAYER:
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
    public void addDirectory() {
        settingsPanel.openFileChooser();
    }
    public void refreshDirectoryList() {
        settingsPanel.refreshDirectoryList();
    }
    public void pullSongs() {
        playerPanel.getMusicPanel().setData(model.getSongs());
    }
    public void pullMetadata() {
        playerPanel.getBottomBar().setSongTitle(model.getTitle());
        playerPanel.getBottomBar().setAlbum(model.getAlbum());
        playerPanel.getBottomBar().setAlbumArt(new ImageIcon(new ImageIcon(model.getArtworkBytes()).getImage().getScaledInstance(ALBUM_IMG_SIZE, ALBUM_IMG_SIZE, Image.SCALE_SMOOTH)));
        playerPanel.getBottomBar().setTotalTime(model.getLength(), model.getSeconds());
        playerPanel.getMusicPanel().setRowSelection(model.getIndex());
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
        return playerPanel.getMusicPanel().rowAtPoint(point);
    }
    public int convertRowIndexToModel(int row) {
        return playerPanel.getMusicPanel().convertRowIndexToModel(row);
    }

    // setters
    public void setRowFilter(RowFilter<Object,Object> regexFilter) {
        playerPanel.getMusicPanel().getTableSorter().setRowFilter(regexFilter);
    }
    public void setPlayback(String string) {
        playerPanel.getBottomBar().setPlayback(string);
    }

    // static
    static {
        ALBUM_IMG_SIZE = 80;
    }
}
