import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class TopBarPanel extends JPanel {
    private Model model;
    public TopBarPanel(Model model) {
        super();

        this.model = model;

        createLayout();
        addComponents();
    }

    private void createLayout() {
        this.setLayout(new FlowLayout(FlowLayout.RIGHT));
    }

    JButton settingsButton;
    private void addComponents() {
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        settingsButton.setActionCommand("settings");

        this.add(settingsButton);
    }
}
