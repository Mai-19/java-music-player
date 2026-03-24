package view.components;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.SearchListener;
import view.Icons;
import view.View;

/**
 * class with search bar and settings button
 */
public class TopBarPanel extends JPanel {
    private final View view;

    /**
     * constructor for the top bar panel class
     * @param view
     */
    public TopBarPanel(View view) {
        super();

        this.view = view;

        setBackground(View.FOREGROUND);
        createLayout();
        addComponents();
        registerControllers();
    }

    /**
     * creates the layout for the top bar panel class
     */
    private void createLayout() {
        this.setLayout(new BorderLayout());
    }
    
    private MusicPlayerButton settingsButton;
    private JTextField searchField;
    private JLabel searchIcon;
    /**
     * adds components to the panel
     */
    private void addComponents() {
        settingsButton = new MusicPlayerButton(Icons.SETTINGS_SLIDERS);
        searchIcon = new JLabel(Icons.SEARCH);
        settingsButton.setActionCommand("settings");

        searchField = new JTextField(30);
        searchField.setBorder(new RoundedBorder(20, 7));
        searchField.setToolTipText("Search");
        searchField.setBackground(getBackground());

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        centerPanel.add(searchIcon);
        centerPanel.add(searchField);

        JLabel label = new JLabel(Icons.MUSIC_25);
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        this.add(label, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(settingsButton, BorderLayout.EAST);
    }

    /**
     * registers the controllers for the panel
     */
    public void registerControllers() {
        searchField.addCaretListener(new SearchListener(view));
    }

    // getters
    public MusicPlayerButton getSettingsButton() {
        return settingsButton;
    }
}
