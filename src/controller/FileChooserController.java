package controller;

import javax.swing.JFileChooser;

import model.Model;
import view.components.SettingsPanel;

/**
 * Controller for the directory file chooser in the settings panel
 */
public class FileChooserController {

    private final Model model;
    private final SettingsPanel settingsPanel;

    public FileChooserController(Model model, SettingsPanel settingsPanel) {
        this.model = model;
        this.settingsPanel = settingsPanel;
    }

    /**
     * Opens the file chooser and adds the selected directory to the model
     * @return true if a directory was selected and added
     */
    public boolean openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(settingsPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            model.addDirectory(chooser.getSelectedFile().getAbsolutePath());
            settingsPanel.refreshDirectoryList();
        }
        return result == JFileChooser.APPROVE_OPTION;
    }
}
