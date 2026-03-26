package model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager handles all communication with the SQLite database
 * 
 * it is responsible for creating and connecting to the database file
 * and for all read and write operations across every table
 * 
 * the database file is stored in the users home directory under
 * JavaMusicPlayer-Data so no installation or configuration is needed
 * 
 * tables managed by this class:
 *   DIRECTORIES   - music folders the user has added
 *   SONGS         - all scanned songs and their metadata
 *   PLAYLISTS     - user created playlists
 *   PLAYLIST_SONGS - which songs belong to which playlist
 *   STATS         - play counts and last played dates per song
 *   META          - key value store for app level settings
 */
public class DatabaseManager {

    // path to the folder that holds the database file
    private static final String DB_FOLDER = System.getProperty("user.home") + "/JavaMusicPlayer-Data";
    // full path to the sqlite file inside that folder
    private static final String DB_FILE = DB_FOLDER + "/MusicPlayerDB.sqlite";
    // jdbc connection string used by DriverManager
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    // the active connection to the database
    private Connection connection;

    /**
     * initializes the database connection and creates all tables
     * 
     * creates the data folder if it does not exist yet
     * then opens or creates the sqlite file at that path
     * enables foreign key enforcement and runs CREATE TABLE IF NOT EXISTS
     * for every table so the schema is always up to date on first launch
     * 
     * @return true if the connection succeeded - false if anything failed
     */
    public boolean init() {
        try {
            Files.createDirectories(Paths.get(DB_FOLDER));
            connection = DriverManager.getConnection(URL);

            // enforce foreign key constraints - sqlite disables these by default
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            createTables();
            System.out.println("Connected to SQLite database.");
            return true;
        } catch (Exception e) {
            System.out.println("Database init failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * closes the database connection cleanly
     * called when the application exits
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Disconnected from database.");
            }
        } catch (SQLException e) {
            System.out.println("Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * returns the raw connection object
     * only needed if another class needs to run a custom query
     * 
     * @return the active Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * creates all tables if they do not already exist
     * 
     * uses CREATE TABLE IF NOT EXISTS so this is safe to call
     * every time the app starts without losing any existing data
     * 
     * foreign keys are set up so that deleting a directory
     * also deletes its songs and deleting a song removes it
     * from all playlists and its stats row automatically
     * 
     * @throws SQLException if any table creation statement fails
     */
    private void createTables() throws SQLException {
        // stores the absolute paths of folders the user has added
        String createDirectories = "CREATE TABLE IF NOT EXISTS DIRECTORIES (" +
                "    path TEXT PRIMARY KEY" +
                ")";

        // stores every scanned song with its metadata and a link back to its directory
        String createSongs = "CREATE TABLE IF NOT EXISTS SONGS (" +
                "    song_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "    title TEXT, " +
                "    artist TEXT, " +
                "    album TEXT, " +
                "    release_year TEXT, " +
                "    seconds INTEGER, " +
                "    length TEXT, " +
                "    path TEXT NOT NULL UNIQUE, " +
                "    directory_path TEXT NOT NULL, " +
                "    last_modified INTEGER, " +
                "    FOREIGN KEY (directory_path) REFERENCES DIRECTORIES(path) ON DELETE CASCADE" +
                ")";

        // one row per song tracking total plays and when it was last played
        String createStats = "CREATE TABLE IF NOT EXISTS STATS (" +
                "    song_id INTEGER PRIMARY KEY, " +
                "    play_count INTEGER NOT NULL DEFAULT 0, " +
                "    last_played TEXT, " +
                "    FOREIGN KEY (song_id) REFERENCES SONGS(song_id) ON DELETE CASCADE" +
                ")";

        // key value store
        // tracks when stats were last reset
        // tracks volume bar state
        String createMeta = "CREATE TABLE IF NOT EXISTS META (" +
                "    key TEXT PRIMARY KEY, " +
                "    value TEXT" +
                ")";

        // stores user created playlists - name must be unique
        String createPlaylists = "CREATE TABLE IF NOT EXISTS PLAYLISTS (" +
                "    playlist_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "    name TEXT NOT NULL UNIQUE" +
                ")";

        // junction table linking songs to playlists - composite primary key prevents duplicates
        String createPlaylistSongs = "CREATE TABLE IF NOT EXISTS PLAYLIST_SONGS (" +
                "    playlist_id INTEGER, " +
                "    song_id INTEGER, " +
                "    PRIMARY KEY (playlist_id, song_id), " +
                "    FOREIGN KEY (playlist_id) REFERENCES PLAYLISTS(playlist_id) ON DELETE CASCADE, " +
                "    FOREIGN KEY (song_id) REFERENCES SONGS(song_id) ON DELETE CASCADE" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createDirectories);
            stmt.executeUpdate(createSongs);
            stmt.executeUpdate(createStats);
            stmt.executeUpdate(createMeta);
            stmt.executeUpdate(createPlaylists);
            stmt.executeUpdate(createPlaylistSongs);
        }
    }

    // ─── DIRECTORIES ───────────────────────────────────────────────

    /**
     * saves a new directory path to the database
     * uses INSERT OR IGNORE so adding the same path twice is safe
     * 
     * @param path absolute path to the music folder
     */
    public void addDirectory(String path) {
        String sql = "INSERT OR IGNORE INTO DIRECTORIES (path) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding directory: " + e.getMessage());
        }
    }

    /**
     * removes a directory from the database
     * because of the ON DELETE CASCADE on SONGS this also deletes
     * every song that was scanned from that directory
     * 
     * @param path absolute path of the directory to remove
     */
    public void removeDirectory(String path) {
        String sql = "DELETE FROM DIRECTORIES WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing directory: " + e.getMessage());
        }
    }

    /**
     * returns all saved directory paths sorted alphabetically
     * called on startup so the model knows which folders to scan
     * 
     * @return list of absolute directory path strings
     */
    public List<String> loadDirectories() {
        List<String> dirs = new ArrayList<>();
        String sql = "SELECT path FROM DIRECTORIES ORDER BY path";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dirs.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading directories: " + e.getMessage());
        }

        return dirs;
    }

    // ─── SONGS ─────────────────────────────────────────────────────

    /**
     * inserts a new song or updates it if the path already exists
     * 
     * uses the ON CONFLICT DO UPDATE pattern so a rescan will
     * refresh metadata for songs that have changed on disk
     * without creating duplicate rows
     * 
     * @param song          the song object containing metadata to save
     * @param directoryPath the directory this song was scanned from
     */
    public void addOrUpdateSong(Song song, String directoryPath) {
        String sql = "INSERT INTO SONGS " +
                "(title, artist, album, release_year, seconds, length, path, directory_path, last_modified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(path) DO UPDATE SET " +
                "title = excluded.title, " +
                "artist = excluded.artist, " +
                "album = excluded.album, " +
                "release_year = excluded.release_year, " +
                "seconds = excluded.seconds, " +
                "length = excluded.length, " +
                "directory_path = excluded.directory_path, " +
                "last_modified = excluded.last_modified";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Object[] info = song.getInfo();

            stmt.setString(1, safeString(info[0])); // title
            stmt.setString(2, safeString(info[1])); // artist
            stmt.setString(3, safeString(info[2])); // album
            stmt.setString(4, safeString(info[3])); // year
            stmt.setInt(5, song.getSeconds());       // seconds
            stmt.setString(6, safeString(info[4])); // length
            stmt.setString(7, song.getPath());       // path
            stmt.setString(8, directoryPath);        // saved directory
            stmt.setLong(9, getFileLastModified(song.getPath()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding/updating song: " + e.getMessage());
        }
    }

    /**
     * convenience wrapper that guesses the directory from the songs path
     * used when only a Song object is available and the directory is not known separately
     * 
     * @param song the song to save
     */
    public void addSong(Song song) {
        String guessedDirectory = getParentDirectory(song.getPath());
        addOrUpdateSong(song, guessedDirectory);
    }

    /**
     * returns all songs in the database sorted alphabetically by title
     * this is the full library load called on startup and after rescans
     * 
     * @return list of Song objects for every row in SONGS
     */
    public List<Song> loadSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT title, artist, album, release_year, seconds, length, path FROM SONGS ORDER BY artist, album COLLATE NOCASE";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                if (Path.of(rs.getString("path")).toFile().exists()) {
                    songs.add(new Song(
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getString("release_year"),
                        rs.getInt("seconds"),
                        rs.getString("length"),
                        rs.getString("path")));
                } else {removeSongByPath(rs.getString("path"));}
            }
        } catch (SQLException e) {
            System.out.println("Error loading songs: " + e.getMessage());
        }

        return songs;
    }

    /**
     * returns only the songs that were scanned from a specific directory
     * used when removing a directory to clean up its songs from the queue
     * 
     * @param directoryPath the directory to filter by
     * @return list of Song objects belonging to that directory
     */
    public List<Song> loadSongsForDirectory(String directoryPath) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT title, artist, album, release_year, seconds, length, path " +
                "FROM SONGS WHERE directory_path = ? ORDER BY title COLLATE NOCASE";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, directoryPath);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(new Song(
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("album"),
                            rs.getString("release_year"),
                            rs.getInt("seconds"),
                            rs.getString("length"),
                            rs.getString("path")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading songs for directory: " + e.getMessage());
        }

        return songs;
    }

    /**
     * deletes a single song by its file path
     * used if a file is detected as missing during a rescan
     * 
     * @param path absolute file path of the song to remove
     */
    public void removeSongByPath(String path) {
        String sql = "DELETE FROM SONGS WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing song: " + e.getMessage());
        }
    }

    /**
     * deletes all songs that belong to a given directory
     * called before removing the directory itself so the
     * cascade does not need to be relied upon in all cases
     * 
     * @param directoryPath directory whose songs should be removed
     */
    public void removeSongsForDirectory(String directoryPath) {
        String sql = "DELETE FROM SONGS WHERE directory_path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, directoryPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing songs for directory: " + e.getMessage());
        }
    }

    /**
     * checks whether a song with the given file path already exists in the database
     * used during scanning to skip files that have not changed
     * 
     * @param path absolute file path to check
     * @return true if the song exists - false otherwise
     */
    public boolean songExists(String path) {
        String sql = "SELECT 1 FROM SONGS WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking song existence: " + e.getMessage());
            return false;
        }
    }

    // ─── STATS ─────────────────────────────────────────────────────

    /**
     * records that a song has been played
     * 
     * if the song has never been played before a new row is inserted
     * if it has been played before the play count is incremented
     * and last_played is updated to the current time
     * 
     * @param path absolute file path of the song that was played
     */
    public void recordPlay(String path) {
        int songId = getSongId(path);
        if (songId == -1)
            return;

        String sql = "INSERT INTO STATS (song_id, play_count, last_played) " +
                "VALUES (?, 1, datetime('now')) " +
                "ON CONFLICT(song_id) DO UPDATE SET " +
                "play_count = play_count + 1, " +
                "last_played = datetime('now')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error recording play: " + e.getMessage());
        }
    }

    /**
     * returns the total number of times a song has been played
     * returns 0 if the song has never been played or does not exist
     * 
     * @param path absolute file path of the song
     * @return play count as an integer
     */
    public int getPlayCount(String path) {
        int songId = getSongId(path);
        if (songId == -1)
            return 0;

        String sql = "SELECT play_count FROM STATS WHERE song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, songId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("play_count");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting play count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * returns the most played songs ordered by play count descending
     * used by exportTopSongsImage to generate the weekly stats graphic
     * 
     * each entry in the returned list is a String array with three values:
     *   index 0 - song title
     *   index 1 - artist name
     *   index 2 - play count as a string
     * 
     * @param limit maximum number of songs to return
     * @return list of string arrays each containing title artist and play count
     */
    public List<String[]> getTopSongs(int limit) {
        List<String[]> results = new ArrayList<>();

        String sql = "SELECT s.title, s.artist, st.play_count " +
                "FROM SONGS s " +
                "JOIN STATS st ON s.song_id = st.song_id " +
                "ORDER BY st.play_count DESC, s.title ASC " +
                "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[] {
                            rs.getString("title"),
                            rs.getString("artist"),
                            String.valueOf(rs.getInt("play_count"))
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting top songs: " + e.getMessage());
        }

        return results;
    }

    // ─── HELPERS ───────────────────────────────────────────────────

    /**
     * looks up the internal song_id for a given file path
     * used internally by stats and playlist methods that need the id
     * 
     * @param path absolute file path of the song
     * @return the song_id integer or -1 if the song is not found
     */
    private int getSongId(String path) {
        String sql = "SELECT song_id FROM SONGS WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("song_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting song ID: " + e.getMessage());
        }

        return -1;
    }

    /**
     * reads the last modified timestamp of a file on disk in milliseconds
     * stored alongside each song so we can detect if a file has changed
     * 
     * @param filePath absolute path to the file
     * @return last modified time in milliseconds or 0 if the file cannot be read
     */
    private long getFileLastModified(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.getLastModifiedTime(path).toMillis();
            }
        } catch (Exception e) {
            System.out.println("Error reading last modified time: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * returns the parent folder path of a file
     * used as a fallback when a directoryPath is not explicitly provided
     * 
     * @param filePath absolute path to the file
     * @return parent directory path or empty string if it cannot be determined
     */
    private String getParentDirectory(String filePath) {
        try {
            Path parent = Paths.get(filePath).getParent();
            return parent == null ? "" : parent.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * safely converts an object to a string
     * returns an empty string instead of null to avoid SQL errors
     * 
     * @param value the object to convert
     * @return string representation or empty string if null
     */
    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * checks if a week has passed since the last stats reset
     * if so it clears all play counts and updates the reset date in META
     * 
     * called on startup so stats are always based on the current week
     */
    public void resetStatsIfNewWeek() {
        String lastReset = getMeta("last_reset");
        LocalDate now = LocalDate.now();

        // reset if there is no recorded date or if more than a week has passed
        if (lastReset == null || LocalDate.parse(lastReset).plusWeeks(1).isBefore(now)) {
            resetStats();
            setMeta("last_reset", now.toString());
            System.out.println("Weekly stats reset.");
        }
    }

    /**
     * sets all play counts to zero and clears last_played for every song
     * called automatically by resetStatsIfNewWeek when a week rolls over
     */
    public void resetStats() {
        String sql = "UPDATE STATS SET play_count = 0, last_played = NULL";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error resetting stats: " + e.getMessage());
        }
    }

    /**
     * saves or updates a key value pair in the META table
     * used to persist small pieces of app state like the last reset date
     * 
     * @param key   the key to store the value under
     * @param value the value to store
     */
    private void setMeta(String key, String value) {
        String sql = "INSERT INTO META (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error setting meta: " + e.getMessage());
        }
    }

    /**
     * retrieves a value from the META table by its key
     * returns null if the key does not exist
     * 
     * @param key the key to look up
     * @return the stored value or null if not found
     */
    private String getMeta(String key) {
        String sql = "SELECT value FROM META WHERE key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("value");
            }
        } catch (SQLException e) {
            System.out.println("Error getting meta: " + e.getMessage());
        }
        return null;
    }

    // ─── PLAYLISTS ─────────────────────────────────────────────────

    /**
     * creates a new playlist with the given name
     * uses INSERT OR IGNORE so creating the same name twice does nothing
     * 
     * @param name the display name for the new playlist
     */
    public void createPlaylist(String name) {
        String sql = "INSERT OR IGNORE INTO PLAYLISTS (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creating playlist: " + e.getMessage());
        }
    }

    /**
     * deletes a playlist by name
     * cascade deletes remove the corresponding rows in PLAYLIST_SONGS automatically
     * 
     * @param name the name of the playlist to delete
     */
    public void deletePlaylist(String name) {
        String sql = "DELETE FROM PLAYLISTS WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
        }
    }

    /**
     * returns the names of all playlists sorted alphabetically
     * used to populate the playlists panel and the right click context menu
     * 
     * @return list of playlist name strings
     */
    public List<String> loadPlaylists() {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT name FROM PLAYLISTS ORDER BY name COLLATE NOCASE";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading playlists: " + e.getMessage());
        }
        return playlists;
    }

    /**
     * adds a song to a playlist
     * uses INSERT OR IGNORE so adding the same song twice does nothing
     * 
     * returns early if either the playlist or song cannot be found in the database
     * 
     * @param playlistName the name of the target playlist
     * @param songPath     absolute file path of the song to add
     */
    public void addSongToPlaylist(String playlistName, String songPath) {
        int playlistId = getPlaylistId(playlistName);
        int songId = getSongId(songPath);
        if (playlistId == -1 || songId == -1)
            return;

        String sql = "INSERT OR IGNORE INTO PLAYLIST_SONGS (playlist_id, song_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding song to playlist: " + e.getMessage());
        }
    }

    /**
     * removes a song from a playlist
     * only removes the link in PLAYLIST_SONGS - the song itself is not deleted
     * 
     * @param playlistName the name of the playlist to remove the song from
     * @param songPath     absolute file path of the song to remove
     */
    public void removeSongFromPlaylist(String playlistName, String songPath) {
        int playlistId = getPlaylistId(playlistName);
        int songId = getSongId(songPath);
        if (playlistId == -1 || songId == -1)
            return;

        String sql = "DELETE FROM PLAYLIST_SONGS WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing song from playlist: " + e.getMessage());
        }
    }

    /**
     * returns all songs in a given playlist sorted alphabetically by title
     * joins SONGS and PLAYLIST_SONGS to get the full song data
     * 
     * @param playlistName the name of the playlist to load songs for
     * @return list of Song objects in that playlist - empty if not found
     */
    public List<Song> loadSongsForPlaylist(String playlistName) {
        List<Song> songs = new ArrayList<>();
        int playlistId = getPlaylistId(playlistName);
        if (playlistId == -1)
            return songs;

        String sql = "SELECT s.title, s.artist, s.album, s.release_year, s.seconds, s.length, s.path " +
                "FROM SONGS s " +
                "JOIN PLAYLIST_SONGS ps ON s.song_id = ps.song_id " +
                "WHERE ps.playlist_id = ? ORDER BY s.title COLLATE NOCASE";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(new Song(
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("album"),
                            rs.getString("release_year"),
                            rs.getInt("seconds"),
                            rs.getString("length"),
                            rs.getString("path")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading songs for playlist: " + e.getMessage());
        }
        return songs;
    }

    /**
     * looks up the internal playlist_id for a given playlist name
     * used by addSongToPlaylist and removeSongFromPlaylist
     * 
     * @param name the playlist name to look up
     * @return the playlist_id integer or -1 if no playlist with that name exists
     */
    private int getPlaylistId(String name) {
        String sql = "SELECT playlist_id FROM PLAYLISTS WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("playlist_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting playlist ID: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Saves the volume on closing, so it can be restored when the app is reopened
     * @param volume the current volume level
     */
    public void saveVolume(float volume) {
        setMeta("volume", String.valueOf(volume));
    }

    /**
     * loads the volume from the meta table, with a default fallback volume
     * @param defaultVolume default fallback volume in case the table does not have a volume entry
     * @return
     */
    public float loadVolume(float defaultVolume) {
        String val = getMeta("volume");
        return val == null ? defaultVolume : Float.parseFloat(val);
    }
}
