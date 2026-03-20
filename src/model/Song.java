package model;
public class Song {
    private Object[] info;
    private String path;

    public Song(String title, String artist, String album, String year, int seconds, String length, String path) {
        super();

        this.info = new Object[]{title, artist, album, year, seconds, length};
        this.path = path;
    }

    public Object[] getInfo() {
        return info;
    }
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Song{[" +info[0]+", "+info[1]+", "+info[2]+", "+info[3]+", "+info[4]+", "+info[5]+"], "+path+"}";
    }
}
