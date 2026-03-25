package view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Icons is a static utility class that loads and stores all icon images used in the UI
 * 
 * all icons are loaded once at class initialization time via the static block
 * they are stored as public static final fields so any class can reference them
 * without needing to load or scale images themselves
 * 
 * icons are loaded from the classpath under the icons/ resource folder
 * and colored to match the applications text color using colourIcons
 */
public class Icons {

    // playback control icons
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

    // music note icon at multiple sizes used for the window title bar icon
    public static final ImageIcon MUSIC_16;
    public static final ImageIcon MUSIC_25;
    public static final ImageIcon MUSIC_32;
    public static final ImageIcon MUSIC_64;
    public static final ImageIcon MUSIC_256;

    /**
     * loads an image from the classpath and scales it to the given dimensions
     * 
     * uses getClassLoader().getResource so the path works both when running
     * from source and when packaged inside a JAR file
     * 
     * @param path   resource path relative to the classpath root e.g. "icons/play.png"
     * @param width  target width in pixels
     * @param height target height in pixels
     * @return a scaled ImageIcon or null if the image could not be loaded
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

    /**
     * recolors every non-transparent pixel in an icon to the given RGB values
     * preserves the alpha channel so transparent areas stay transparent
     * 
     * used to tint all icons to match the current theme text color
     * 
     * @param icon the source icon to recolor
     * @param r    red component of the target color (0-255)
     * @param g    green component of the target color (0-255)
     * @param b    blue component of the target color (0-255)
     * @return a new ImageIcon with every pixel set to the given color
     */
    public static ImageIcon colourIcons(ImageIcon icon, int r, int g, int b) {
        // draw the icon into a BufferedImage with alpha support
        BufferedImage original = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = original.createGraphics();
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();

        // replace each pixels RGB with the target color while keeping alpha unchanged
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int argb = original.getRGB(x, y);
                int a = (argb >> 24) & 0xFF; // alpha unchanged
                original.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return new ImageIcon(original);
    }

    /**
     * recolors an icon using the current theme text color from View.TEXT
     * this is the standard overload used for all UI icons
     * 
     * @param icon the source icon to recolor
     * @return a new ImageIcon tinted to the theme text color
     */
    public static ImageIcon colourIcons(ImageIcon icon) {
        return colourIcons(icon, View.TEXT.getRed(), View.TEXT.getGreen(), View.TEXT.getBlue());
    }

    // static block loads all icons once when the class is first accessed
    static {
        // blank placeholder used before album art is loaded
        PLACEHOLDER_ALBUM = new ImageIcon(new BufferedImage(View.ALBUM_IMG_SIZE, View.ALBUM_IMG_SIZE, BufferedImage.TYPE_INT_ARGB));

        ADD_DIRECTORY     = colourIcons(load("icons/add-directory.png",    25, 25));
        BACK              = colourIcons(load("icons/back.png",             25, 25));
        FORWARD           = colourIcons(load("icons/forward.png",          25, 25));
        MUTED_VOLUME      = colourIcons(load("icons/muted-volume.png",     25, 25));
        NEXT              = colourIcons(load("icons/next.png",             25, 25));
        PAUSE             = colourIcons(load("icons/pause.png",            25, 25));
        PLAY              = colourIcons(load("icons/play.png",             25, 25));
        PREVIOUS          = colourIcons(load("icons/previous.png",         25, 25));
        REFRESH           = colourIcons(load("icons/refresh.png",          25, 25));
        REPEAT            = colourIcons(load("icons/repeat.png",           25, 25));
        REVERSE           = colourIcons(load("icons/reverse.png",          25, 25));
        SEARCH            = colourIcons(load("icons/search.png",           25, 25));
        SETTINGS_SLIDERS  = colourIcons(load("icons/settings-sliders.png", 25, 25));
        SHUFFLE           = colourIcons(load("icons/shuffle.png",          25, 25));
        TRASH             = colourIcons(load("icons/trash.png",            25, 25));
        VOLUME            = colourIcons(load("icons/volume.png",           25, 25));
        MUSIC_16          = colourIcons(load("icons/music-alt.png",        16, 16));
        MUSIC_25          = colourIcons(load("icons/music-alt.png",        25, 25));
        MUSIC_32          = colourIcons(load("icons/music-alt.png",        32, 32));
        MUSIC_64          = colourIcons(load("icons/music-alt.png",        64, 64));
        MUSIC_256         = colourIcons(load("icons/music-alt.png",       256, 256));
    }
}
