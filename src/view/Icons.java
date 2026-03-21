package view;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * class with icons
 */
public class Icons {
    public static final ImageIcon PLACEHOLDER_ALBUM;
    public static final ImageIcon ADD_DIRECTORY;
    public static final ImageIcon BACK;
    public static final ImageIcon FORWARD;
    public static final ImageIcon MUTED_VOLUME;
    public static final ImageIcon NEXT;
    public static final ImageIcon PAUSE;
    public static final ImageIcon PLAY;
    public static final ImageIcon PREVIOUS;
    public static final ImageIcon REFRESH;
    public static final ImageIcon REPEAT;
    public static final ImageIcon REVERSE;
    public static final ImageIcon SEARCH;
    public static final ImageIcon SETTINGS_SLIDERS;
    public static final ImageIcon SHUFFLE;
    public static final ImageIcon TRASH;
    public static final ImageIcon VOLUME;

    /**
     * Loads an image from the classpath and scales it to a given width and height.
     * @param path
     * @param width
     * @param height
     * @return
     */
    private static ImageIcon load(String path, int width, int height) {
        try {
            var url = Icons.class.getClassLoader().getResource(path);
            return new ImageIcon(ImageIO.read(url).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static {
        PLACEHOLDER_ALBUM = load("placeholder.png", 70, 70);

        ADD_DIRECTORY = load("icons/add-directory.png", 25, 25);
        BACK = load("icons/back.png", 25, 25);
        FORWARD = load("icons/forward.png", 25, 25);
        MUTED_VOLUME = load("icons/muted-volume.png", 25, 25);
        NEXT = load("icons/next.png", 25, 25);
        PAUSE = load("icons/pause.png", 25, 25);
        PLAY = load("icons/play.png", 25, 25);
        PREVIOUS = load("icons/previous.png", 25, 25);
        REFRESH = load("icons/refresh.png", 25, 25);
        REPEAT = load("icons/repeat.png", 25, 25);
        REVERSE = load("icons/reverse.png", 25, 25);
        SEARCH = load("icons/search.png", 25, 25);
        SETTINGS_SLIDERS = load("icons/settings-sliders.png", 25, 25);
        SHUFFLE = load("icons/shuffle.png", 25, 25);
        TRASH = load("icons/trash.png", 25, 25);
        VOLUME = load("icons/volume.png", 25, 25);
    }
}