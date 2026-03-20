package model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;

public class Model {
    // valid extensions
    private String[] musicFileExtensions;

    // lists of songs and directories and the home path
    private HashSet<String> directories;
    private ArrayList<Song> songs;
    private Path directoryObjectPath;

    // audio playback related
    private AudioContext audioContext;
    private SamplePlayer samplePlayer;
    private Gain volumeControlGain;

    // metadata related
    private String title, artist, album, year, length, path;
    private int seconds;
    private byte[] artworkBytes;

    // safety lock for user adjusting time
    private boolean userAdjustingTime;
    private boolean metadataChanged;

    /**
     * TODO: setup a queue, shuffle, and repeat
     * TODO: pull songs from DB on launch
     */

    public Model() {
        super();

        userAdjustingTime = false;
        metadataChanged = false;

        audioContext = AudioContext.getDefaultContext();
        volumeControlGain = new Gain(audioContext, 2, 0.5f);
        audioContext.start();

        directories = new HashSet<>();
        songs = new ArrayList<>();

        musicFileExtensions = new String[]{"mp3", "wav", "flac"};

        Path path = Path.of(System.getProperty("user.home"), "COMP2800-MusicProjectData");
        try {
            Files.createDirectories(path);
            directoryObjectPath = path.resolve("directories.dat");
            if (!Files.exists(directoryObjectPath)) Files.createFile(directoryObjectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadDirectories();
        indexSongs();
    }

    // TODO: replace with database
    public void saveDirectories() { 
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(directoryObjectPath))){
            out.writeObject(directories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: replace with database
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
    }

    public void indexSongs() {
        if (songs != null) songs.clear();
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

    // TODO: add song to database as well.
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

            songs.add(new Song(title, artist, album, year, seconds, length, p.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: add directory to database
    public void addDirectory(String absolutePath) {
        directories.add(absolutePath);
    }

    // TODO: remove directory from database
    public void removeDirectory(String path) {
        directories.remove(path);
    }

    public void play(int row) {
        if (samplePlayer != null) {
            samplePlayer.kill();
        }
        this.getMetadata(row);
        samplePlayer = new SamplePlayer(audioContext, SampleManager.sample(songs.get(row).getPath()));
        volumeControlGain.addInput(samplePlayer);
        audioContext.out.addInput(volumeControlGain);

    }

    private void getMetadata(int row) {
        metadataChanged = true;
        try {
            AudioFile f = AudioFileIO.read(Path.of(songs.get(row).getPath()).toFile());
            Tag tag = f.getTag();
            title  = tag.getFirst(FieldKey.TITLE);
            artist = tag.getFirst(FieldKey.ARTIST);
            album  = tag.getFirst(FieldKey.ALBUM);
            year   = tag.getFirst(FieldKey.YEAR);
            
            AudioHeader header = f.getAudioHeader();
            seconds = header.getTrackLength();
            length = String.format("%d:%02d", seconds / 60, seconds % 60);

            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) artworkBytes = artwork.getBinaryData(); 
            else artworkBytes = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlaybackTime(int time) {
        if (samplePlayer == null) return;
        samplePlayer.setPosition(time*1000);
    }

    public void forwardSong() {
        if (samplePlayer == null) return;
        if (samplePlayer.getPosition() >= (seconds*1000)-5000) nextSong();
        else samplePlayer.setPosition(samplePlayer.getPosition()+5000);
    }
    public void rewindSong() {
        if (samplePlayer == null) return;
        if (samplePlayer.getPosition() <= 5000) samplePlayer.setPosition(0);
        else samplePlayer.setPosition(samplePlayer.getPosition()-5000);
    }

    public void nextSong() {
        if (samplePlayer == null) return;
        // TODO: implement...
    }
    public void previousSong() {
        if (samplePlayer == null) return;
        // TODO: implement...
    }

    public void togglePlayback() {
        if (samplePlayer == null) return;
        samplePlayer.pause(!samplePlayer.isPaused());
    }

    public void pausePlayback() {
        if (samplePlayer == null) return;
        samplePlayer.pause(true);
    }
    
    public void resumePlayback() {
        if (samplePlayer == null) return;
        samplePlayer.pause(false);

    }

    public void setUserAdjustingTime(boolean userAdjustingTime) {
        if (samplePlayer == null) return;
        this.userAdjustingTime = userAdjustingTime;
    }

    // volume needs to be on a logarithmic scale because ears don't perceive volume linearly
    public void setVolume(float value) {
        float linear = value/10f;
        float log = (float) (Math.pow(linear, 2.0));
        volumeControlGain.setGain(log);
    }

    public int getProgress() {
        if (samplePlayer != null && !samplePlayer.isPaused()) {
            return (int) (samplePlayer.getPosition() / 1000);
        }
        return -1; // -1 means don't update
    }

    public boolean hasMetadataChanged() {
        if (metadataChanged) {
            metadataChanged = false; // reset after read
            return true;
        }
        return false;
    }

    public boolean isAdjustingTime() {
        return userAdjustingTime;
    }

    public HashSet<String> getDirectories() {
        return directories;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }

    public String getLength() {
        return length;
    }

    public int getSeconds() {
        return seconds;
    }

    public byte[] getArtworkBytes() {
        return artworkBytes;
    }
}
