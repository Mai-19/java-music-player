import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.CardLayout;
import java.awt.DisplayMode;

public class View {
    private Model model;
    // the screens information, like refresh rate and size
    private DisplayMode screen;

    public View(Model model) {
        super();

        this.model = model;
        
        createFrame();
        registerControllers();
        this.model.addGUI(this);
        update();
    }

    private JFrame frame;
    private void createFrame() {
        // getting the default screen
        screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        frame = new JFrame();

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

    private void addComponents() {
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);

        playerPanel = new PlayerPanel(model);
        settingsPanel = new SettingsPanel(model);

        contentPane.add(playerPanel, Cards.PLAYER.name());
        contentPane.add(settingsPanel, Cards.SETTINGS.name());
        
        frame.setContentPane(contentPane);
    }

    private void registerControllers() {
        ResizeListener resizeListener = new ResizeListener(model);
        ButtonListener buttonListener = new ButtonListener(model);
        ClosingListener closingListener = new ClosingListener(model);
        frame.addWindowListener(closingListener);
        frame.addComponentListener(resizeListener);
        playerPanel.getTopBar().settingsButton.addActionListener(buttonListener);
        settingsPanel.getBackBtn().addActionListener(buttonListener);
    }

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

    public void update() {
        frame.repaint();
    }

    public void addDirectories(HashSet<String> directories) {
        settingsPanel.addDirectories(directories);
    }

    public void updateSongs(ArrayList<Song> songs) {
        playerPanel.getMusicList().setData(songs);
    }

    public void updatePlayingSong(Song song, ImageIcon icon) {
        playerPanel.getBottomBar().setSongTitle((String)song.getInfo()[0]);
        playerPanel.getBottomBar().setAlbumArtist((String)song.getInfo()[1]);
        playerPanel.getBottomBar().setCurrentTime("0:00");
        playerPanel.getBottomBar().setTotalTime((String)song.getInfo()[4]);
        playerPanel.getBottomBar().setAlbumArt(icon);
    }
}
