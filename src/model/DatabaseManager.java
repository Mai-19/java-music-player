package model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager class for the MusicPlayer application.
 */
public class DatabaseManager {

    private static final String DB_FOLDER = System.getProperty("user.home") + "/JavaMusicPlayer-Data";
    private static final String DB_FILE = DB_FOLDER + "/MusicPlayerDB.sqlite";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private Connection connection;

    /**
     * connect to SQLite and create tables
     * 
     * @return
     */
    public boolean init() {
        try {
            Files.createDirectories(Paths.get(DB_FOLDER));
            connection = DriverManager.getConnection(URL);

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

    // disconnect from the database
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

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        String createDirectories = "CREATE TABLE IF NOT EXISTS DIRECTORIES (" +
                "    path TEXT PRIMARY KEY" +
                ")";
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

        String createStats = "CREATE TABLE IF NOT EXISTS STATS (" +
                "    song_id INTEGER PRIMARY KEY, " +
                "    play_count INTEGER NOT NULL DEFAULT 0, " +
                "    last_played TEXT, " +
                "    FOREIGN KEY (song_id) REFERENCES SONGS(song_id) ON DELETE CASCADE" +
                ")";
        String createMeta = "CREATE TABLE IF NOT EXISTS META (" +
                "    key TEXT PRIMARY KEY, " +
                "    value TEXT" +
                ")";
        String createPlaylists = "CREATE TABLE IF NOT EXISTS PLAYLISTS (" +
                "    playlist_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "    name TEXT NOT NULL UNIQUE" +
                ")";
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

    public void addDirectory(String path) {
        String sql = "INSERT OR IGNORE INTO DIRECTORIES (path) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding directory: " + e.getMessage());
        }
    }

    public void removeDirectory(String path) {
        String sql = "DELETE FROM DIRECTORIES WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing directory: " + e.getMessage());
        }
    }

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
            stmt.setInt(5, song.getSeconds()); // seconds
            stmt.setString(6, safeString(info[4])); // length
            stmt.setString(7, song.getPath()); // path
            stmt.setString(8, directoryPath); // saved directory
            stmt.setLong(9, getFileLastModified(song.getPath()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding/updating song: " + e.getMessage());
        }
    }

    // keep this too in case your model already calls addSong(song)
    public void addSong(Song song) {
        String guessedDirectory = getParentDirectory(song.getPath());
        addOrUpdateSong(song, guessedDirectory);
    }

    public List<Song> loadSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT title, artist, album, release_year, seconds, length, path FROM SONGS ORDER BY title COLLATE NOCASE";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

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
        } catch (SQLException e) {
            System.out.println("Error loading songs: " + e.getMessage());
        }

        return songs;
    }

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

    public void removeSongByPath(String path) {
        String sql = "DELETE FROM SONGS WHERE path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing song: " + e.getMessage());
        }
    }

    public void removeSongsForDirectory(String directoryPath) {
        String sql = "DELETE FROM SONGS WHERE directory_path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, directoryPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing songs for directory: " + e.getMessage());
        }
    }

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

    private String getParentDirectory(String filePath) {
        try {
            Path parent = Paths.get(filePath).getParent();
            return parent == null ? "" : parent.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    public void resetStatsIfNewWeek() {
        String lastReset = getMeta("last_reset");
        LocalDate now = LocalDate.now();

        if (lastReset == null || LocalDate.parse(lastReset).plusWeeks(1).isBefore(now)) {
            resetStats();
            setMeta("last_reset", now.toString());
            System.out.println("Weekly stats reset.");
        }
    }

    public void resetStats() {
        String sql = "UPDATE STATS SET play_count = 0, last_played = NULL";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error resetting stats: " + e.getMessage());
        }
    }

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

    public void createPlaylist(String name) {
        String sql = "INSERT OR IGNORE INTO PLAYLISTS (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creating playlist: " + e.getMessage());
        }
    }

    public void deletePlaylist(String name) {
        String sql = "DELETE FROM PLAYLISTS WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
        }
    }

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
}