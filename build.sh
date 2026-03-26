#!/bin/bash
set -e

# Compile
javac -cp "lib/*" -sourcepath src -d bin src/MusicPlayer.java

# Extract dependencies into staging dir
mkdir -p fatjar
for jar in lib/*.jar; do
    unzip -qo "$jar" -d fatjar
done

# Merge SPI files (append, don't overwrite)
mkdir -p fatjar/META-INF/services

printf "org.jflac.sound.spi.FlacAudioFileReader\njavazoom.spi.mpeg.sampled.file.MpegAudioFileReader\n" \
    >> fatjar/META-INF/services/javax.sound.sampled.spi.AudioFileReader

printf "org.jflac.sound.spi.FlacFormatConversionProvider\njavazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider\n" \
    >> fatjar/META-INF/services/javax.sound.sampled.spi.FormatConversionProvider

# Copy your compiled classes and resources into the same staging dir
cp -r bin/. fatjar/
cp -r res/. fatjar/

# Package everything from one source — no duplicate entries
jar cfe MusicPlayer.jar MusicPlayer -C fatjar .

# Cleanup
rm -rf fatjar bin

echo "Done! MusicPlayer.jar is ready."