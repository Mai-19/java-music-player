package controller;

import javax.swing.JFileChooser;

import model.Model;
import view.components.SettingsPanel;

/**
 * FileChooserController handles opening the directory picker in the settings panel
 * 
 * it is a small helper controller that keeps the file chooser logic
 * out of both the view and the ButtonListener
 */
public class FileChooserController {

    private final Model model;
    private final SettingsPanel settingsPanel;

    /**
     * creates the controller with references to the model and the settings panel
     * 
     * @param model         the application model
     * @param settingsPanel the settings panel that triggered the file chooser
     */
    public FileChooserController(Model model, SettingsPanel settingsPanel) {
        this.model = model;
        this.settingsPanel = settingsPanel;
    }

    /**
     * opens a directory picker dialog
     * if the user selects a folder it is added to the model and
     * the directory list in the settings panel is refreshed
     * 
     * @return true if the user selected a directory - false if they cancelled
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
