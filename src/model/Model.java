package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

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

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Model is the central data and logic class for the MusicPlayer application
 * 
 * it follows the Model layer of the MVC pattern - it knows nothing about the
 * UI and communicates with the View only through simple getters and flags
 * 
 * responsibilities:
 *   - managing the song library and the playback queue
 *   - scanning directories for music files using jaudiotagger
 *   - controlling audio playback through the Beads audio engine
 *   - reading and writing all persistent data via DatabaseManager
 *   - parsing LRC lyric files and providing them to the view
 *   - generating the weekly stats image
 */
public class Model {

    // file extensions that are supported for playback
    private String[] musicFileExtensions;

    // the set of directories the user has added
    private HashSet<String> directories;

    // the current list of songs shown in the table - also the playback queue
    private ArrayList<Song> queue;

    // database manager handles all SQLite operations
    private DatabaseManager db;

    // Beads audio engine objects for playback
    private AudioContext audioContext;
    private SamplePlayer samplePlayer;
    private Gain volumeControlGain;

    // metadata for the currently playing song
    private String title, artist, album, year, length;
    private int seconds;
    private byte[] artworkBytes;

    // true while the user is dragging the playback slider
    // prevents the timer from fighting the users input
    private boolean userAdjustingTime;

    // set to true when a new song starts so the view knows to pull fresh metadata
    private boolean metadataChanged;

    // listener that fires nextSong when the current song ends
    private SongEndListener songEndListener;

    // index of the currently playing song in the queue
    private int index;

    // whether shuffle mode is active
    private boolean shuffle;
    
    // linear scale volume value
    private float linearVolume;

    /**
     * creates and initializes the Model
     * 
     * sets up the Beads audio engine with a gain node for volume control
     * initializes the database connection
     * resets weekly stats if a new week has started
     * then loads all saved directories and songs from the database
     */
    public Model() {
        super();

        userAdjustingTime = false;
        metadataChanged = false;
        shuffle = false;

        // create the audio context and chain: samplePlayer -> gain -> output
        audioContext = AudioContext.getDefaultContext();
        volumeControlGain = new Gain(audioContext, 2, 0.5f);
        audioContext.start();
        audioContext.out.addInput(volumeControlGain);

        songEndListener = new SongEndListener(this);

        directories = new HashSet<>();
        queue = new ArrayList<>();
        musicFileExtensions = new String[] { "mp3", "wav", "flac" };

        db = new DatabaseManager();
        db.init();
        setVolume(db.loadVolume(7f));
        db.resetStatsIfNewWeek();

        // load saved data from database only
        loadDirectories();
        loadSongsFromDatabase();
    }

    /**
     * loads the saved directory paths from the database into the local set
     * called on startup and after any directory change
     */
    public void loadDirectories() {
        directories = new HashSet<>(db.loadDirectories());
    }

    /**
     * replaces the current queue with all songs stored in the database
     * called on startup and after a rescan to refresh the song list
     */
    public void loadSongsFromDatabase() {
        queue = new ArrayList<>(db.loadSongs());
    }

    /**
     * rescans all registered directories and updates the database
     * then reloads the queue from the updated database
     * 
     * this is triggered by the refresh button in settings
     */
    public void indexSongs() {
        for (String dir : directories) {
            indexDirectory(dir);
        }
        loadSongsFromDatabase();
    }

    /**
     * walks a single directory recursively and adds every music file found
     * skips files that are not a supported audio format
     * 
     * @param directoryPath absolute path of the directory to scan
     */
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

    /**
     * checks if a file has a supported audio extension
     * 
     * @param p the file path to check
     * @return true if the file ends with mp3 wav or flac
     */
    private boolean isMusicFile(Path p) {
        String name = p.toString().toLowerCase();
        for (String ext : musicFileExtensions) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * reads the audio metadata from a file and saves it to the database
     * 
     * uses jaudiotagger to extract the title artist album year and duration
     * skips 24-bit audio files because the playback library does not support them
     * 
     * @param p             the path of the audio file to add
     * @param directoryPath the directory this file was found in
     */
    public void addSong(Path p, String directoryPath) {
        try {
            AudioFile f = AudioFileIO.read(p.toFile());
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();

            // 24-bit files are not supported by the audio engine
            if (header.getBitsPerSample() > 16) {
                System.err.println("Skipping Unsupported 24-bit file: " + p);
                return;
            }

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

    /**
     * adds a directory to the local set and saves it to the database
     * does nothing if the directory is already registered
     * 
     * @param absolutePath absolute path of the directory to add
     */
    public void addDirectory(String absolutePath) {
        if (directories.add(absolutePath)) {
            db.addDirectory(absolutePath);
        }
    }

    /**
     * removes a directory and all its songs from the database and the queue
     * 
     * @param path absolute path of the directory to remove
     */
    public void removeDirectory(String path) {
        if (directories.remove(path)) {
            db.removeSongsForDirectory(path);
            db.removeDirectory(path);
            loadSongsFromDatabase();
        }
    }

    /**
     * starts playing the song at the given queue index
     * 
     * stops any currently playing song first
     * loads the audio sample via the Beads SampleManager
     * attaches the end listener so the next song plays automatically
     * records the play in the database for stats tracking
     * pre-caches the next song in the background to reduce loading lag
     * 
     * @param row the index in the queue to play
     */
    public void play(int row) {
        if (samplePlayer != null) {
            // remove the end listener before killing so nextSong is not triggered
            samplePlayer.setKillListener(null);
            samplePlayer.kill();
        }

        volumeControlGain.clearInputConnections();

        getMetadata(row);
        samplePlayer = new SamplePlayer(audioContext, SampleManager.sample(queue.get(row).getPath()));
        samplePlayer.setKillListener(songEndListener);
        index = row;
        volumeControlGain.addInput(samplePlayer);

        db.recordPlay(queue.get(row).getPath());

        precacheNext();
    }

    /**
     * reads the metadata and album art for the song at the given queue index
     * sets metadataChanged to true so the view knows to refresh the display
     * 
     * @param row the index in the queue to read metadata from
     */
    private void getMetadata(int row) {
        metadataChanged = true;
        try {
            AudioFile f = AudioFileIO.read(Path.of(queue.get(row).getPath()).toFile());
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

            // read the album art bytes for display in the bottom bar
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

    /**
     * jumps playback to the given time position
     * the SamplePlayer uses milliseconds so seconds are multiplied by 1000
     * 
     * @param time the target position in seconds
     */
    public void setPlaybackTime(int time) {
        if (samplePlayer == null)
            return;
        samplePlayer.setPosition(time * 1000);
    }

    /**
     * skips forward by 5 seconds
     * if less than 5 seconds remain in the song the next song is played instead
     */
    public void forwardSong() {
        if (samplePlayer == null)
            return;
        if (samplePlayer.getPosition() >= (seconds * 1000) - 5000)
            nextSong();
        else
            samplePlayer.setPosition(samplePlayer.getPosition() + 5000);
    }

    /**
     * skips backwards by 5 seconds
     * if the playback position is already within the first 5 seconds
     * it rewinds to the start instead
     */
    public void rewindSong() {
        if (samplePlayer == null)
            return;
        if (samplePlayer.getPosition() <= 5000)
            samplePlayer.setPosition(0);
        else
            samplePlayer.setPosition(samplePlayer.getPosition() - 5000);
    }

    /**
     * advances to the next song in the queue
     * 
     * if repeat is enabled it restarts the current song instead
     * wraps around to the first song if the end of the queue is reached
     */
    public void nextSong() {
        if (samplePlayer == null)
            return;
        if (queue.isEmpty())
            return;
        if (samplePlayer.getLoopType() == SamplePlayer.LoopType.LOOP_FORWARDS) {
            setPlaybackTime(0);
            return;
        }
        index = (index + 1 + queue.size()) % queue.size();
        play(index);
    }

    /**
     * goes back to the previous song in the queue
     * 
     * if more than 5 seconds have played it restarts the current song instead
     * wraps around to the last song if already at the beginning of the queue
     */
    public void previousSong() {
        if (samplePlayer == null)
            return;
        if (queue.isEmpty())
            return;
        if (getProgress() > 5) {
            setPlaybackTime(0);
            return;
        }
        index = (index - 1 + queue.size()) % queue.size();
        play(index);
    }

    /**
     * toggles playback between paused and playing
     */
    public void togglePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(!samplePlayer.isPaused());
    }

    /**
     * pauses the current song without stopping it
     * used by the playback slider while the user drags it
     */
    public void pausePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(true);
    }

    /**
     * resumes playback after a pause
     * used by the playback slider after the user lets go
     */
    public void resumePlayback() {
        if (samplePlayer == null)
            return;
        samplePlayer.pause(false);
    }

    /**
     * sets the flag that indicates the user is currently dragging the slider
     * while this flag is true the timer does not update the slider position
     * so the users input is not overwritten
     * 
     * @param userAdjustingTime true when the slider is being dragged
     */
    public void setUserAdjustingTime(boolean userAdjustingTime) {
        if (samplePlayer == null)
            return;
        this.userAdjustingTime = userAdjustingTime;
    }

    /**
     * sets the playback volume using a logarithmic scale
     * 
     * the input value is from the slider range divided by 10
     * it is then squared to create a more natural sounding volume curve
     * where small movements near zero have less impact than near the top
     * 
     * @param value the raw slider value
     */
    public void setVolume(float value) {
        linearVolume = value;
        float log = (float) Math.pow(linearVolume / 10f, 2.0);
        db.saveVolume(value);
        volumeControlGain.setGain(log);
    }

    /**
     * returns the current playback position in whole seconds
     * returns -1 if nothing is playing or the player is paused
     * 
     * @return playback position in seconds or -1
     */
    public int getProgress() {
        if (samplePlayer != null && !samplePlayer.isPaused()) {
            return (int) (samplePlayer.getPosition() / 1000);
        }
        return -1;
    }

    /**
     * returns true if a new song has started since the last time this was checked
     * the view calls this on each timer tick to know when to refresh the metadata display
     * 
     * @return true if metadata has changed
     */
    public boolean hasMetadataChanged() {
        if (metadataChanged) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the user is currently dragging the playback slider
     * 
     * @return true if time adjustment is in progress
     */
    public boolean isAdjustingTime() {
        return userAdjustingTime;
    }

    /**
     * returns the set of registered music directory paths
     * 
     * @return set of directory path strings
     */
    public HashSet<String> getDirectories() {
        return directories;
    }

    /**
     * returns the current playback queue
     * this is also the list shown in the song table
     * 
     * @return list of Song objects in the current queue order
     */
    public ArrayList<Song> getQueue() {
        return queue;
    }

    /** @return title of the currently playing song */
    public String getTitle() { return title; }

    /** @return artist of the currently playing song */
    public String getArtist() { return artist; }

    /** @return album of the currently playing song */
    public String getAlbum() { return album; }

    /** @return release year of the currently playing song */
    public String getYear() { return year; }

    /** @return formatted length string of the currently playing song */
    public String getLength() { return length; }

    /** @return duration in seconds of the currently playing song */
    public int getSeconds() { return seconds; }

    /**
     * returns the raw bytes of the album art image for the current song
     * null if the current song has no embedded artwork
     * 
     * @return byte array of the album art or null
     */
    public byte[] getArtworkBytes() {
        return artworkBytes;
    }

    /**
     * returns the DatabaseManager instance
     * exposed so the view can access playlist and stats operations directly
     * 
     * @return the active DatabaseManager
     */
    public DatabaseManager getDb() {
        return db;
    }

    /**
     * trims whitespace from a tag value and returns empty string instead of null
     * prevents null pointer errors when a tag field is missing from the audio file
     * 
     * @param value raw string from the audio tag
     * @return trimmed string or empty string if null
     */
    private String safeTagValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * toggles shuffle mode on and off
     * 
     * when turning shuffle on the queue is randomly reordered
     * when turning it off the queue is reloaded from the database in original order
     */
    public void shuffleSongs() {
        shuffle = !shuffle;
        if (shuffle)
            Collections.shuffle(queue);
        else
            queue = new ArrayList<>(db.loadSongs());
    }

    /**
     * toggles repeat mode for the current song
     * 
     * when repeat is on the SamplePlayer loops the current song indefinitely
     * when turned off it returns to normal forward playback
     */
    public void repeatSong() {
        if (samplePlayer == null)
            return;
        samplePlayer.setLoopType(
                samplePlayer.getLoopType() == SamplePlayer.LoopType.NO_LOOP_FORWARDS
                        ? SamplePlayer.LoopType.LOOP_FORWARDS
                        : SamplePlayer.LoopType.NO_LOOP_FORWARDS);
    }

    /**
     * loads the next song into memory in a background thread
     * 
     * the SampleManager cache is limited to 2 samples at a time
     * so the oldest sample is removed before caching the next one
     * this reduces the delay when the current song ends and the next one starts
     */
    public void precacheNext() {
        if (SampleManager.getSampleNameList().size() > 2)
            SampleManager.removeSample(SampleManager.getSampleNameList().get(0));
        if (queue.isEmpty())
            return;
        int nextIndex = (index + 1) % queue.size();
        new Thread(() -> {
            SampleManager.sample(queue.get(nextIndex).getPath());
        }).start();
    }

    /**
     * returns the index of the currently playing song in the queue
     * 
     * @return current queue index
     */
    public int getIndex() {
        return index;
    }

    /**
     * generates and saves a PNG image showing the top 5 most played songs
     * 
     * draws a dark background with a header subtitle divider and ranked list
     * each entry shows the rank number title artist and play count
     * the image is 1200x840 pixels suitable for sharing
     * 
     * @param outputPath absolute path where the PNG should be saved
     * @throws Exception if the image cannot be written to disk
     */
    public void exportTopSongsImage(String outputPath) throws Exception {
        List<String[]> topSongs = db.getTopSongs(5);

        int width = 1200;
        int height = 840;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, width, height);

        // header
        g.setColor(new Color(180, 120, 255));
        g.setFont(new Font("SansSerif", Font.BOLD, 56));
        g.drawString("Weekly Stats", 80, 110);

        // subtitle
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g.drawString("Your top 5 most played songs", 80, 160);

        // divider
        g.setColor(new Color(70, 70, 70));
        g.fillRect(80, 190, width - 160, 4);

        // draw each song entry with rank number title artist and play count
        int y = 280;
        for (int i = 0; i < topSongs.size(); i++) {
            String[] song = topSongs.get(i);
            String title = song[0].isEmpty() ? "Unknown Title" : song[0];
            String artist = song[1].isEmpty() ? "Unknown Artist" : song[1];
            String plays = song[2] + (song[2].equals("1") ? " play" : " plays");

            // rank number
            g.setColor(new Color(180, 120, 255));
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString("#" + (i + 1), 80, y);

            // title
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 32));
            g.drawString(truncate(title, 45), 160, y);

            // artist and play count on the line below
            g.setColor(new Color(150, 150, 150));
            g.setFont(new Font("SansSerif", Font.PLAIN, 26));
            g.drawString(truncate(artist, 45) + "  •  " + plays, 160, y + 40);

            y += 120;
        }

        // centered footer
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth("MusicPlayer");
        g.drawString("MusicPlayer", (width - textWidth) / 2, height - 20);

        g.dispose();

        ImageIO.write(image, "png", new File(outputPath));
        System.out.println("Saved to " + outputPath);
    }

    /**
     * truncates a string to a maximum number of characters
     * appends an ellipsis if the string was shortened
     * used to prevent long song or artist names from overflowing the stats image
     * 
     * @param text     the string to truncate
     * @param maxChars the maximum allowed length
     * @return the original string or a shortened version ending with an ellipsis
     */
    private String truncate(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars - 1) + "…" : text;
    }

    /**
     * creates a new playlist by delegating to the database
     * 
     * @param name the name for the new playlist
     */
    public void createPlaylist(String name) {
        db.createPlaylist(name);
    }

    /**
     * deletes a playlist by delegating to the database
     * 
     * @param name the name of the playlist to delete
     */
    public void deletePlaylist(String name) {
        db.deletePlaylist(name);
    }

    /**
     * returns all playlist names from the database
     * 
     * @return list of playlist name strings
     */
    public List<String> loadPlaylists() {
        return db.loadPlaylists();
    }

    /**
     * adds a song to a playlist by delegating to the database
     * 
     * @param playlistName the target playlist
     * @param songPath     absolute file path of the song to add
     */
    public void addSongToPlaylist(String playlistName, String songPath) {
        db.addSongToPlaylist(playlistName, songPath);
    }

    /**
     * removes a song from a playlist by delegating to the database
     * 
     * @param playlistName the playlist to remove the song from
     * @param songPath     absolute file path of the song to remove
     */
    public void removeSongFromPlaylist(String playlistName, String songPath) {
        db.removeSongFromPlaylist(playlistName, songPath);
    }

    /**
     * loads all songs belonging to a given playlist from the database
     * 
     * @param playlistName the playlist to load songs for
     * @return list of Song objects in that playlist
     */
    public List<Song> loadSongsForPlaylist(String playlistName) {
        return db.loadSongsForPlaylist(playlistName);
    }

    /**
     * returns the current song list - same as getQueue
     * used by the view and controllers that prefer this name
     * 
     * @return list of songs currently in the queue
     */
    public List<Song> getSongs() {
        return queue;
    }

    /**
     * replaces the entire queue with a new list of songs
     * used when opening a playlist to swap out the displayed songs
     * 
     * @param songsForPlaylist the new list of songs to display
     */
    public void setSongs(List<Song> songsForPlaylist) {
        queue = new ArrayList<Song>(songsForPlaylist);
    }

    /**
     * parses an LRC lyric file associated with the given audio file path
     * 
     * looks for a file with the same name as the audio file but with a .lrc extension
     * in the same directory
     * 
     * supports both synced LRC format with timestamps like [mm:ss.xx]
     * and unsynced plain text files where each line gets a fake 5 second timestamp
     * 
     * @param audioPath absolute path to the audio file
     * @return a TreeMap of timestamp milliseconds to lyric line text
     *         or null if no LRC file was found or the file was empty
     */
    public TreeMap<Long, String> parseLrc(String audioPath) {
        // swap the audio extension for .lrc
        String lrcPath = audioPath.replaceAll("\\.[^.]+$", ".lrc");
        Path path = Path.of(lrcPath);

        if (!Files.exists(path))
            return null;

        List<String> rawLines;
        try {
            rawLines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        TreeMap<Long, String> result = new TreeMap<>();
        // regex matches [mm:ss.xx] or [mm:ss:xx] at the start of a line
        java.util.regex.Pattern timestampPattern = java.util.regex.Pattern
                .compile("^\\[(\\d{2}):(\\d{2})[.：:](\\d{2,3})\\](.*)$");

        boolean hasSyncedLines = false;

        for (String line : rawLines) {
            // skip metadata tags like [ar:Artist] [ti:Title] etc
            if (line.matches("^\\[[a-zA-Z]+:.*\\]$"))
                continue;

            java.util.regex.Matcher m = timestampPattern.matcher(line.trim());
            if (m.matches()) {
                hasSyncedLines = true;
                int minutes = Integer.parseInt(m.group(1));
                int seconds = Integer.parseInt(m.group(2));
                String msStr = m.group(3);

                // normalise to ms: 2-digit = centiseconds x10  3-digit = already ms
                long ms = msStr.length() == 2
                        ? (minutes * 60L + seconds) * 1000 + Integer.parseInt(msStr) * 10L
                        : (minutes * 60L + seconds) * 1000 + Integer.parseInt(msStr);

                String text = m.group(4).trim();
                if (!text.isEmpty())
                    result.put(ms, text);
            }
        }

        // unsynced fallback: assign evenly spaced fake timestamps so all lines are shown
        if (!hasSyncedLines) {
            long fakeMs = 0;
            for (String line : rawLines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    result.put(fakeMs, trimmed);
                    fakeMs += 5000; // 5 seconds apart
                }
            }
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * clears the metadata changed flag after the view has read the latest metadata
     * called by the view after pullMetadata completes
     */
    public void markMetadataRetrieved() {
        metadataChanged = false;
    }

    public float getVolume() {
        return linearVolume;
    }
}
