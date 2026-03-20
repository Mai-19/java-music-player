package view;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Icons {
    public static final ImageIcon PLACEHOLDER_ALBUM = load("placeholder.png", 70, 70);

    public static final ImageIcon ADD_DIRECTORY = load("icons/add-directory.png", 25, 25);
    public static final ImageIcon BACK = load("icons/back.png", 25, 25);
    public static final ImageIcon FORWARD = load("icons/forward.png", 25, 25);
    public static final ImageIcon MUTED_VOLUME = load("icons/muted-volume.png", 25, 25);
    public static final ImageIcon NEXT = load("icons/next.png", 25, 25);
    public static final ImageIcon PAUSE = load("icons/pause.png", 25, 25);
    public static final ImageIcon PLAY = load("icons/play.png", 25, 25);
    public static final ImageIcon PREVIOUS = load("icons/previous.png", 25, 25);
    public static final ImageIcon REFRESH = load("icons/refresh.png", 25, 25);
    public static final ImageIcon REPEAT = load("icons/repeat.png", 25, 25);
    public static final ImageIcon REVERSE = load("icons/reverse.png", 25, 25);
    public static final ImageIcon SEARCH = load("icons/search.png", 25, 25);
    public static final ImageIcon SETTINGS_SLIDERS = load("icons/settings-sliders.png", 25, 25);
    public static final ImageIcon SHUFFLE = load("icons/shuffle.png", 25, 25);
    public static final ImageIcon TRASH = load("icons/trash.png", 25, 25);
    public static final ImageIcon VOLUME = load("icons/volume.png", 25, 25);

    private static ImageIcon load(String path, int width, int height) {
        try {
            var url = Icons.class.getClassLoader().getResource(path);
            return new ImageIcon(ImageIO.read(url).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}