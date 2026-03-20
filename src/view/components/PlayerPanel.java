package view.components;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.Model;
import view.View;

public class PlayerPanel extends JPanel {
    private Model model;
    private View view;

    public PlayerPanel(Model model, View view) {
        super();

        this.model = model;
        this.view = view;
        createLayout();
    }

    private TopBarPanel topBar;

    private MusicPanel musicPanel;

    private BottomBarPanel bottomBar;
    private void createLayout() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topBar = new TopBarPanel(view);

        musicPanel = new MusicPanel(model, view);
        RoundedPanel rounded = new RoundedPanel(musicPanel, 60);

        bottomBar = new BottomBarPanel(model);

        this.add(topBar, BorderLayout.NORTH);
        this.add(rounded, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);
    }

    public TopBarPanel getTopBar() {
        return topBar;
    }
    public MusicPanel getMusicPanel() {
        return musicPanel;
    }
    public BottomBarPanel getBottomBar() {
        return bottomBar;
    }
}
