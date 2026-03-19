import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BottomBarPanel extends JPanel {

    private Model model;

    private JLabel albumArtLabel;
    private ScrollingLabel songTitleLabel;
    private ScrollingLabel albumArtistLabel;
    private JSlider progressBar;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JSlider volumeSlider;
    private MusicPlayerButton prevBtn, rewindBtn, playPauseBtn, forwardBtn, nextBtn;
    private MusicPlayerButton volumeBtn;
    private MusicPlayerToggleButton shuffleBtn, repeatBtn;

    private int unMuteVolume;
    private boolean muteFlag;

    public BottomBarPanel(Model model) {
        super();
        this.model = model;
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setPreferredSize(new Dimension(0, 90));

        add(buildAlbumArtAndInfo(), BorderLayout.WEST);
        add(buildCenterControls(), BorderLayout.CENTER);
        add(buildVolumePanel(), BorderLayout.EAST);
    }

    private JPanel buildAlbumArtAndInfo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        albumArtLabel = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("placeholder.png")).getImage().getScaledInstance(68, 68, Image.SCALE_SMOOTH)));
        albumArtLabel.setPreferredSize(new Dimension(68, 68));

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

    private JPanel buildCenterControls() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonsRow.setOpaque(false);

        try {
            shuffleBtn = new MusicPlayerToggleButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/shuffle.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
            prevBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/backward-fast.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
            rewindBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/step-backward.png"))).getScaledInstance(19, 19, BufferedImage.SCALE_SMOOTH)));
            playPauseBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/play-pause.png"))).getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH)));
            forwardBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/step-forward.png"))).getScaledInstance(19, 19, BufferedImage.SCALE_SMOOTH)));
            nextBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/forward-fast.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
            repeatBtn = new MusicPlayerToggleButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/arrows-repeat.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        progressBar = new JSlider(0, 100, 0);
        progressBar.setFocusable(false);

        progressBar.addChangeListener(e -> {
            if (progressBar.getValueIsAdjusting()) {
                model.pausePlayback();
                model.setUserAdjustingTime(true);
                model.setPlaybackTime(progressBar.getValue()*1000);
            }
            else {
                model.setUserAdjustingTime(false);
                model.resumePlayback();
            }
        });

        progressRow.add(currentTimeLabel, BorderLayout.WEST);
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(totalTimeLabel, BorderLayout.EAST);

        panel.add(buttonsRow, BorderLayout.NORTH);
        panel.add(progressRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildVolumePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(160, 60));

        try {
            volumeBtn = new MusicPlayerButton(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/volume.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        volumeSlider = new JSlider(0, 10, 7);
        volumeSlider.setPreferredSize(new Dimension(90, 20));
        volumeSlider.setFocusable(false);

        volumeSlider.addChangeListener(e -> {
            if (volumeSlider.getValueIsAdjusting()) {
                model.setVolume(volumeSlider.getValue());
                if (volumeSlider.getValue() == 0) {
                    try {
                        volumeBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/volume-slash.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        volumeBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/volume.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        muteFlag = true;
        volumeBtn.addActionListener(e -> {
            if (muteFlag) {
                unMuteVolume = volumeSlider.getValue();
                volumeSlider.setValue(0);
                model.setVolume(0);
                try {
                    volumeBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/volume-slash.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                muteFlag = false;
            } else {
                volumeSlider.setValue(unMuteVolume);
                try {
                    volumeBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/volume.png"))).getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                model.setVolume(unMuteVolume);
                muteFlag = true;
            }
        });

        panel.add(volumeBtn);
        panel.add(volumeSlider);
        return panel;
    }

    public void addActionListener(ActionListener actionListener) {
        rewindBtn.addActionListener(actionListener);
        playPauseBtn.addActionListener(actionListener);
        forwardBtn.addActionListener(actionListener);
    }

    public void setPlaybackButtonIcon(String string) {
        try {
            if (string.equals("play")) playPauseBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/play.png"))).getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH)));
            else if (string.equals("pause")) playPauseBtn.setIcon(new ImageIcon((ImageIO.read(this.getClass().getResource("/icons/pause.png"))).getScaledInstance(25, 25, BufferedImage.SCALE_SMOOTH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getProgress() { return progressBar.getValue(); }

    public void setSongTitle(String title) { songTitleLabel.setScrollingText(title); }
    public void setAlbumArtist(String text) { albumArtistLabel.setScrollingText(text); }
    public void setCurrentTime(String time) { currentTimeLabel.setText(time); }
    public void setAlbumArt(ImageIcon icon) { albumArtLabel.setIcon(icon); }
    public void setTotalTime(String time) { 
        totalTimeLabel.setText(time);
        progressBar.setMaximum((Integer.parseInt(time.split(":")[0])*60) + (Integer.parseInt(time.split(":")[1])));
    }

    public void setProgress(int value) { progressBar.setValue(value); }
}