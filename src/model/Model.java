package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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

    // lists of songs and directories
    private HashSet<String> directories;
    private ArrayList<Song> songs;

    // database manager
    private DatabaseManager db;

    // audio playback related
    private AudioContext audioContext;
    private SamplePlayer samplePlayer;
    private Gain volumeControlGain;

    // metadata related
    private String title, artist, album, year, length;
    private int seconds;
    private byte[] artworkBytes;

    // safety lock for user adjusting time
    private boolean userAdjustingTime;
    private boolean metadataChanged;

    // end song listener
    private SongEndListener songEndListener;
    private int index;

    private boolean shuffle;

    public Model() {
        super();

        userAdjustingTime = false;
        metadataChanged = false;
        shuffle = false;

        audioContext = AudioContext.getDefaultContext();
        volumeControlGain = new Gain(audioContext, 2, 0.5f);
        audioContext.start();
        audioContext.out.addInput(volumeControlGain);
        songEndListener = new SongEndListener(this);

        directories = new HashSet<>();
        songs = new ArrayList<>();

        musicFileExtensions = new String[] { "mp3", "wav", "flac" };

        db = new DatabaseManager();
        db.init();

        // load saved data from database only
        loadDirectories();
        loadSongsFromDatabase();
    }

    public void loadDirectories() {
        directories = new HashSet<>(db.loadDirectories());
    }

    public void loadSongsFromDatabase() {
        songs = new ArrayList<>(db.loadSongs());
    }

    // manual refresh / reindex
    public void indexSongs() {
        for (String dir : directories) {
            indexDirectory(dir);
        }
        loadSongsFromDatabase();
    }

    // scan one directory only
    public void indexDirectory(String directoryPath) {
        try {
            Files.walk(Path.of(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(this::isMusicFile)
                    .forEach(p -> addSong(p, directoryPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isMusicFile(Path p) {
        String name = p.toString().toLowerCase();
        for (String ext : musicFileExtensions) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    // add or update song in database
    public void addSong(Path p, String directoryPath) {
        try {
            AudioFile f = AudioFileIO.read(p.toFile());
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();

            String title = "";
            String artist = "";
            String album = "";
            String year = "";

            if (tag != null) {
                title = safeTagValue(tag.getFirst(FieldKey.TITLE));
                artist = safeTagValue(tag.getFirst(FieldKey.ARTIST));
                album = safeTagValue(tag.getFirst(FieldKey.ALBUM));
                year = safeTagValue(tag.getFirst(FieldKey.YEAR));
            }

            int seconds = header.getTrackLength();
            String length = String.format("%d:%02d", seconds / 60, seconds % 60);

            Song song = new Song(title, artist, album, year, seconds, length, p.toString());
            db.addOrUpdateSong(song, directoryPath);
        } catch (Exception e) {
            System.err.println("Failed to add song: " + p);
            e.printStackTrace();
        }
    }

    public void addDirectory(String absolutePath) {
        if (directories.add(absolutePath)) {
            db.addDirectory(absolutePath);
            indexDirectory(absolutePath);
            loadSongsFromDatabase();
        }
    }

    public void removeDirectory(String path) {
        if (directories.remove(path)) {
            db.removeSongsForDirectory(path);
            db.removeDirectory(path);
            loadSongsFromDatabase();
        }
    }

    public void play(int row) {
        if (samplePlayer != null) {
            samplePlayer.setKillListener(null);
            samplePlayer.kill();
        }

        volumeControlGain.clearInputConnections();

        getMetadata(row);
        samplePlayer = new SamplePlayer(audioContext, SampleManager.sample(songs.get(row).getPath()));
        samplePlayer.setKillListener(songEndListener);
        index = row;
        volumeControlGain.addInput(samplePlayer);

        db.recordPlay(songs.get(row).getPath());

        precacheNext();
    }

    private void getMetadata(int row) {
        metadataChanged = true;
        try {
            AudioFile f = AudioFileIO.read(Path.of(songs.get(row).getPath()).toFile());
            Tag tag = f.getTag();

            title = "";
            artist = "";
            album = "";
            year = "";

            if (tag != null) {
                title = safeTagValue(tag.getFirst(FieldKey.TITLE));
                artist = safeTagValue(tag.getFirst(FieldKey.ARTIST));
                album = safeTagValue(tag.getFirst(FieldKey.ALBUM));
                year = safeTagValue(tag.getFirst(FieldKey.YEAR));
            }

            AudioHeader header = f.getAudioHeader();
            seconds = header.getTrackLength();
            length = String.format("%d:%02d", seconds / 60, seconds % 60);

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    artworkBytes = artwork.getBinaryData();
                } else {
                    artworkBytes = null;
                }
            } else {
                artworkBytes = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlaybackTime(int time) {
        if (samplePlayer == null)
            return;
        samplePlayer.setPosition(time * 1000);
    }

    public void forwardSong() {
        if (samplePlayer == null)
            return;
        if (samplePlayer.getPosition() >= (seconds * 1000) - 5000)
            nextSong();
        else
            samplePlayer.setPosition(samplePlayer.getPosition() + 5000);
    }

    public void rewindSong() {
        if (samplePlayer == null)
            return;
        if (samplePlayer.getPosition() <= 5000)
            samplePlayer.setPosition(0);
        else
            samplePlayer.setPosition(samplePlayer.getPosition() - 5000);
    }

    public void nextSong() {
        if (samplePlayer == null)
            return;
        if (songs.isEmpty())
            return;
        index = (index + 1 + songs.size()) % songs.size();
        play(index);
    }

    public void previousSong() {
        if (samplePlayer == null)
            return;
        if (songs.isEmpty())
            return;
        index = (index - 1 + songs.size()) % songs.size();
        play(index);
    }

    public void togglePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(!samplePlayer.isPaused());
    }

    public void pausePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(true);
    }

    public void resumePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(false);
    }

    public void setUserAdjustingTime(boolean userAdjustingTime) {
        if (samplePlayer == null)
            return;
        this.userAdjustingTime = userAdjustingTime;
    }

    public void setVolume(float value) {
        float linear = value / 10f;
        float log = (float) Math.pow(linear, 2.0);
        volumeControlGain.setGain(log);
    }

    public int getProgress() {
        if (samplePlayer != null && !samplePlayer.isPaused()) {
            return (int) (samplePlayer.getPosition() / 1000);
        }
        return -1;
    }

    public boolean hasMetadataChanged() {
        if (metadataChanged) {
            metadataChanged = false;
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

    public DatabaseManager getDb() {
        return db;
    }

    private String safeTagValue(String value) {
        return value == null ? "" : value.trim();
    }

    public void shuffleSongs() {
        shuffle = !shuffle;
        if (shuffle)
            Collections.shuffle(songs);
        else
            songs = new ArrayList<>(db.loadSongs());
    }

    public void repeatSong() {
        if (samplePlayer == null)
            return;
        samplePlayer.setLoopType(
                samplePlayer.getLoopType() == SamplePlayer.LoopType.NO_LOOP_FORWARDS
                        ? SamplePlayer.LoopType.LOOP_FORWARDS
                        : SamplePlayer.LoopType.NO_LOOP_FORWARDS);
    }

    public void precacheNext() {
        if (songs.isEmpty())
            return;
        int nextIndex = (index + 1) % songs.size();
        new Thread(() -> {
            SampleManager.sample(songs.get(nextIndex).getPath());
        }).start();
    }

    public int getIndex() {
        return index;
    }
}