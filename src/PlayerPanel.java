import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PlayerPanel extends JPanel {
    private Model model;

    public PlayerPanel(Model model) {
        super();

        this.model = model;
        createLayout();
    }

    private TopBarPanel topBar;

    private MusicPanel musicList;

    private BottomBarPanel bottomBar;
    private void createLayout() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topBar = new TopBarPanel(model);

        musicList = new MusicPanel(model);

        bottomBar = new BottomBarPanel(model);

        this.add(topBar, BorderLayout.NORTH);
        this.add(musicList, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);
    }

    public TopBarPanel getTopBar() {
        return topBar;
    }
    public MusicPanel getMusicList() {
        return musicList;
    }
    public BottomBarPanel getBottomBar() {
        return bottomBar;
    }
}
