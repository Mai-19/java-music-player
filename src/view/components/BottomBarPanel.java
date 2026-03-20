package view.components;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.PlaybackSliderListener;
import controller.VolumeSliderListener;
import model.Model;
import view.Icons;
import view.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

/**
 * Class for the bottom bar panel <br>
 * contains the playback controls <br>
 * and the album art label
 */
public class BottomBarPanel extends JPanel {

    private Model model;
    private View view;

    private JLabel albumArtLabel;
    // custom label, scrolls if text is too long
    private ScrollingLabel songTitleLabel;
    private ScrollingLabel albumArtistLabel;
    // custom sliders, themed
    private MusicPlayerSlider progressBar;
    private MusicPlayerSlider volumeSlider;

    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    // custom buttons, themed
    private MusicPlayerButton prevBtn, rewindBtn, playPauseBtn, forwardBtn, nextBtn;
    private MusicPlayerButton volumeBtn;
    private MusicPlayerToggleButton shuffleBtn, repeatBtn;

    // saved volume for un-mute event
    private int unMuteVolume;
    private boolean muteFlag;

    /**
     * constructor for the BottomBarPanel class
     * @param model
     */
    public BottomBarPanel(Model model, View view) {
        super();
        this.model = model;
        this.view = view;
        setLayout(new BorderLayout(10, 0));
        // padding border
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setPreferredSize(new Dimension(0, 90));

        add(buildAlbumArtAndInfo(), BorderLayout.WEST);
        add(buildCenterControls(), BorderLayout.CENTER);
        add(buildVolumePanel(), BorderLayout.EAST);
    }

    /**
     * creates album art and scrolling label panel
     * @return JPanel
     */
    private JPanel buildAlbumArtAndInfo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        // placeholder image
        albumArtLabel = new JLabel(Icons.PLACEHOLDER_ALBUM);
        albumArtLabel.setPreferredSize(new Dimension(70, 70));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        songTitleLabel = new ScrollingLabel();
        albumArtistLabel = new ScrollingLabel();

        infoPanel.add(songTitleLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(albumArtistLabel);

        panel.add(albumArtLabel);
        panel.add(infoPanel);
        return panel;
    }

    /**
     * creates the center controls panel
     * @return
     */
    private JPanel buildCenterControls() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonsRow.setOpaque(false);

        shuffleBtn = new MusicPlayerToggleButton(Icons.SHUFFLE);
        prevBtn = new MusicPlayerButton(Icons.PREVIOUS);
        rewindBtn = new MusicPlayerButton(Icons.REVERSE);
        playPauseBtn = new MusicPlayerButton(Icons.PLAY);
        forwardBtn = new MusicPlayerButton(Icons.FORWARD);
        nextBtn = new MusicPlayerButton(Icons.NEXT);
        repeatBtn = new MusicPlayerToggleButton(Icons.REPEAT);

        rewindBtn.setActionCommand("rewind");
        playPauseBtn.setActionCommand("toggle playback");
        forwardBtn.setActionCommand("forward");

        buttonsRow.add(shuffleBtn);
        buttonsRow.add(prevBtn);
        buttonsRow.add(rewindBtn);
        buttonsRow.add(playPauseBtn);
        buttonsRow.add(forwardBtn);
        buttonsRow.add(nextBtn);
        buttonsRow.add(repeatBtn);

        JPanel progressRow = new JPanel(new BorderLayout(6, 0));
        progressRow.setOpaque(false);

        currentTimeLabel = new JLabel("0:00");
        totalTimeLabel = new JLabel("0:00");
        progressBar = new MusicPlayerSlider(0, 100, 0);
        progressBar.setFocusable(false);

        progressBar.addChangeListener(new PlaybackSliderListener(model, view));

        progressRow.add(currentTimeLabel, BorderLayout.WEST);
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(totalTimeLabel, BorderLayout.EAST);

        panel.add(buttonsRow, BorderLayout.NORTH);
        panel.add(progressRow, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the volume panel
     * @return
     */
    private JPanel buildVolumePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(160, 60));

        volumeBtn = new MusicPlayerButton(Icons.VOLUME);
        volumeSlider = new MusicPlayerSlider(0, 10, 7);
        volumeSlider.setPreferredSize(new Dimension(90, 20));
        volumeSlider.setFocusable(false);

        volumeSlider.addChangeListener(new VolumeSliderListener(model, view));

        volumeBtn.setActionCommand("toggle mute");
        
        muteFlag = true;

        panel.add(volumeBtn);
        panel.add(volumeSlider);
        return panel;
    }

    // adds action listeners to the controls
    public void addActionListener(ActionListener actionListener) {
        rewindBtn.addActionListener(actionListener);
        playPauseBtn.addActionListener(actionListener);
        forwardBtn.addActionListener(actionListener);
        volumeBtn.addActionListener(actionListener);
    }

    // toggles the play buttons icon
    public void togglePlayback() {
        if (playPauseBtn.getIcon().equals(Icons.PLAY)) playPauseBtn.setIcon(Icons.PAUSE);
        else playPauseBtn.setIcon(Icons.PLAY);
    }

    /**
     * toggles the mute icon and mutes the volume.
     * on second click it resets the volume to its previous position
     */
    public void toggleMute() {
        if (muteFlag) {
            unMuteVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);
            model.setVolume(0);
            volumeBtn.setIcon(Icons.MUTED_VOLUME);
            muteFlag = false;
        } else {
            volumeSlider.setValue(unMuteVolume);
            volumeBtn.setIcon(Icons.VOLUME);
            model.setVolume(unMuteVolume);
            muteFlag = true;
        }
    }

    // getters
    public int getProgress() { return progressBar.getValue(); }

    // setters
    public void setSongTitle(String title) { songTitleLabel.setScrollingText(title); }
    public void setAlbum(String text) { albumArtistLabel.setScrollingText(text); }
    public void setCurrentTime(String time) { currentTimeLabel.setText(time); }
    public void setAlbumArt(ImageIcon icon) { albumArtLabel.setIcon(icon); }
    public void setTotalTime(String time, int seconds) { 
        totalTimeLabel.setText(time);
        progressBar.setMaximum(seconds);
    }
    public void setProgress(int value) { progressBar.setValue(value); }
    public void setVolume(float value) {
        if (value == 0) {
            volumeBtn.setIcon(Icons.MUTED_VOLUME);
        } else {
            volumeBtn.setIcon(Icons.VOLUME);
        }
    }
    public void setPlayback(String string) {
        if (string.equals("play")) playPauseBtn.setIcon(Icons.PLAY);
        else if (string.equals("pause")) playPauseBtn.setIcon(Icons.PAUSE);
    }
}