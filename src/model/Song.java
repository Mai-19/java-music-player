package model;

/**
 * Song represents a single music file in the library
 * 
 * it stores the songs metadata (title artist album year length)
 * in an object array so it can be passed directly into the JTable
 * as a row without any conversion
 * 
 * the path and seconds fields are stored separately because
 * they are used for playback and timing but are not shown in the table
 */
public class Song {

    // info holds: title artist album year length - in that order
    // this order must match the column order of the table in MusicPanel
    private Object[] info;

    // absolute file path to the song on disk
    private String path;

    // duration in whole seconds - used to set the slider maximum
    private int seconds;

    /**
     * creates a Song with all its metadata
     * 
     * @param title   song title from the audio file tag
     * @param artist  artist name from the audio file tag
     * @param album   album name from the audio file tag
     * @param year    release year from the audio file tag
     * @param seconds total duration in seconds
     * @param length  formatted duration string like "3:45"
     * @param path    absolute file path to the audio file
     */
    public Song(String title, String artist, String album, String year, int seconds, String length, String path) {
        super();
        // store display fields in array for easy table row insertion
        this.info = new Object[]{title, artist, album, year, length};
        this.seconds = seconds;
        this.path = path;
    }

    /**
     * returns the metadata array
     * used by MusicPanel to populate a row in the song table
     * order is: title artist album year length
     * 
     * @return object array of display fields
     */
    public Object[] getInfo() {
        return info;
    }

    /**
     * returns the absolute file path to the audio file
     * used by Model to load and play the song
     * 
     * @return absolute path string
     */
    public String getPath() {
        return path;
    }

    /**
     * returns the duration in whole seconds
     * used to set the maximum value on the playback slider
     * 
     * @return duration in seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * returns a readable summary of the song for debugging
     * includes all metadata fields and the file path
     */
    @Override
    public String toString() {
        return "Song{[" + info[0] + ", " + info[1] + ", " + info[2] + ", " + info[3] + ", " + info[4] + "], " + path + "}";
    }
}
