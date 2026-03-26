@echo off
setlocal enabledelayedexpansion
set ERRORLEVEL=0

echo Compiling source...
mkdir bin 2>nul
javac -cp "lib/*" -sourcepath src -d bin src/MusicPlayer.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    exit /b 1
)

echo Extracting dependencies...
mkdir fatjar 2>nul
cd fatjar
for %%j in (..\lib\*.jar) do (
    jar xf "%%j"
)
cd ..

echo Merging SPI files...
mkdir fatjar\META-INF\services 2>nul
>> fatjar\META-INF\services\javax.sound.sampled.spi.AudioFileReader (
    echo org.jflac.sound.spi.FlacAudioFileReader
    echo javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
)
>> fatjar\META-INF\services\javax.sound.sampled.spi.FormatConversionProvider (
    echo org.jflac.sound.spi.FlacFormatConversionProvider
    echo javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider
)

echo Copying compiled classes and resources...
xcopy /s /q bin\* fatjar\ >nul
xcopy /s /q res\* fatjar\ >nul

echo Packaging JAR...
jar cfe MusicPlayer.jar MusicPlayer -C fatjar .

echo Cleaning up...
rmdir /s /q fatjar
rmdir /s /q bin

echo Done! MusicPlayer.jar is ready.