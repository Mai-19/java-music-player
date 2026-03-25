package app;
import model.Model;
import view.View;

/**
 * Main entry point for the MusicPlayer application
 * 
 * this class starts the app by creating the model and view
 * the model holds all data and logic
 * the view builds the window and listens to the model
 */
public class MusicPlayer {

    /**
     * main method - starts the application
     * 
     * sets two system properties before anything else to make
     * text render smoothly instead of appearing pixelated on some systems
     * 
     * @param args command line arguments - not used
     * @throws Exception if the model or view fail to initialize
     */
    public static void main(String[] args) throws Exception {
        // these are to stop the text from appearing "pixelated"
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // init of model and view
        Model model = new Model();
        View view = new View(model);
        view.update();
    }
}
