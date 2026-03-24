# MusicPlayer
A lightweight all-java local music player for FLAC, MP3, and WAV.

## Features
- FLAC, MP3, and WAV playback
- Playlists
- Lyrics from LRC files (both synced and not synced)
- Multiple directory scanning
- All normal playback features (loop song, shuffle queue, forward, reverse, next track, previous track, volume controls)
- A Shareable weekly stats image

## Installation

### Prerequisites
- [OpenJDK 25](https://jdk.java.net/25/) <br> (recommended, will work for Java 17 and up)

### Precompiled Steps
1. Download the latest release [here](https://github.com/Mai-19/comp-2800-project/releases/latest)
2. Run with `java -jar MusicPlayer.jar` OR double click OR right click and select run with java

### Build Steps
1. **Clone the repository**
```bash
git clone https://github.com/Mai-19/comp-2800-project.git
cd comp-2800-project
```
2. **Compile the source**
All dependencies are included in `lib/`. Run the following from the project root:
```bash
javac -cp "lib/*" -sourcepath src -d out src/app/App.java
```
3. **Package into JAR**
```bash
jar cfe MusicPlayer.jar app.App -C out . -C res .
```
4. **Run**
```bash
java -jar MusicPlayer.jar
```

## Usage
### Lyrics using LRC
drop a .lrc file into the same directory as the song file and it will be auto-detected.
LRC file must have the same name as the song file

If you have `really-good-song.flac` the LRC file must be `really-good-song.lrc`

For more info on [LRC](https://en.wikipedia.org/wiki/LRC_(file_format))

### Getting the weekly stats image
go to settings and press the "Download Stats" button at the bottom of the page

### Adding a directory
Add a directory in the settings and press refresh!

![Demo](.github/assets/demo.gif)

## Screenshots
<p float="left">
  <img src=".github/assets/all-songs.png" width=49%>
  <img src=".github/assets/playlists-list.png" width=49%>
  <img src=".github/assets/playlist-view.png" width=49%>
  <img src=".github/assets/lyrics.png" width=49%>
  <img src=".github/assets/settings.png">
</p>
<img src=".github/assets/stats.png">

## License
[LGPL-3.0](./LICENSE)
