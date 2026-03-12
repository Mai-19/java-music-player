import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.ImageIcon;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

public class Model {
    private View view;

    private String[] musicFileExtensions;

    private HashSet<String> directories;
    private ArrayList<Song> songs;
    private Path directoryObjectPath;
    public Model() {
        super();

        directories = new HashSet<>();
        songs = new ArrayList<>();

        musicFileExtensions = new String[]{"mp3", "wav", "ogg"};

        Path path = Path.of(System.getProperty("user.home"), "COMP2800-MusicProjectData");
        try {
            Files.createDirectories(path);
            directoryObjectPath = path.resolve("directories.dat");
            if (!Files.exists(directoryObjectPath)) Files.createFile(directoryObjectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGUI(View view) {
        this.view = view;
        loadDirectories();
        indexSongs();
        view.updateSongs(songs);
    }

    public void update() {
        view.update();
    }

    public void changeView(Cards settings){
        view.changeView(settings);
    }

    public void saveDirectories() {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(directoryObjectPath))){
            out.writeObject(directories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadDirectories() {
        try {
            if (Files.size(directoryObjectPath) > 0) {
                try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(directoryObjectPath))){
                    directories = (HashSet<String>)in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        view.addDirectories(directories);
    }

    public void indexSongs() {
        for (String dir : directories) {
            try {
                Files.walk(Path.of(dir))
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.toString().toLowerCase();
                        for (String ext : musicFileExtensions)
                            if (name.endsWith("." + ext)) return true;
                        return false;
                    })
                    .forEach(p -> addSong(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addSong(Path p) {
        try {
            AudioFile f = AudioFileIO.read(p.toFile());
            Tag tag = f.getTag();
            String title  = tag.getFirst(FieldKey.TITLE);
            String artist = tag.getFirst(FieldKey.ARTIST);
            String album  = tag.getFirst(FieldKey.ALBUM);
            String year   = tag.getFirst(FieldKey.YEAR);
            AudioHeader header = f.getAudioHeader();
            int seconds = header.getTrackLength();
            String length = String.format("%d:%02d", seconds / 60, seconds % 60);

            songs.add(new Song(title, artist, album, year, length, p.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDirectory(String absolutePath) {
        directories.add(absolutePath);
    }

    public void removeDirectory(String path) {
        directories.remove(path);
    }

    public void play(int row) {
        try {
            AudioFile f = AudioFileIO.read(Path.of(songs.get(row).getPath()).toFile());
            Tag tag = f.getTag();
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageData = artwork.getBinaryData();
                ImageIcon icon = new ImageIcon(imageData);
                view.updatePlayingSong(songs.get(row), new ImageIcon(icon.getImage().getScaledInstance(68, 68, Image.SCALE_SMOOTH)));
            } else {
                ImageIcon albumArtLabel = new ImageIcon(new ImageIcon(getClass().getResource("placeholder.png")).getImage().getScaledInstance(68, 68, Image.SCALE_SMOOTH));
                view.updatePlayingSong(songs.get(row), albumArtLabel);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
