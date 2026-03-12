import javax.swing.*;
import java.awt.*;

public class BottomBarPanel extends JPanel {

    private Model model;

    private JLabel albumArtLabel;
    private JLabel songTitleLabel;
    private JLabel albumArtistLabel;
    private JSlider progressBar;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JSlider volumeSlider;
    private JButton shuffleBtn, prevBtn, rewindBtn, playPauseBtn, forwardBtn, nextBtn, repeatBtn;
    private JButton volumeBtn;

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

        songTitleLabel = new JLabel("Song Title");
        albumArtistLabel = new JLabel("Album  •  Artist");

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

        shuffleBtn   = new JButton("Shuffle");
        prevBtn      = new JButton("Prev");
        rewindBtn    = new JButton("-10s");
        playPauseBtn = new JButton("Play");
        forwardBtn   = new JButton("+10s");
        nextBtn      = new JButton("Next");
        repeatBtn    = new JButton("Repeat");

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

        volumeBtn = new JButton("Vol");
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(90, 20));

        panel.add(volumeBtn);
        panel.add(volumeSlider);
        return panel;
    }

    public void setSongTitle(String title)    { songTitleLabel.setText(title); }
    public void setAlbumArtist(String text)   { albumArtistLabel.setText(text); }
    public void setProgress(int value)        { progressBar.setValue(value); }
    public void setCurrentTime(String time)   { currentTimeLabel.setText(time); }
    public void setTotalTime(String time)     { totalTimeLabel.setText(time); }
    public void setVolume(int value)          { volumeSlider.setValue(value); }
    public void setPlayPauseText(String text) { playPauseBtn.setText(text); }
    public void setAlbumArt(ImageIcon icon)   { albumArtLabel.setIcon(icon); }

    public JButton getShuffleBtn()   { return shuffleBtn; }
    public JButton getPrevBtn()      { return prevBtn; }
    public JButton getRewindBtn()    { return rewindBtn; }
    public JButton getPlayPauseBtn() { return playPauseBtn; }
    public JButton getForwardBtn()   { return forwardBtn; }
    public JButton getNextBtn()      { return nextBtn; }
    public JButton getRepeatBtn()    { return repeatBtn; }
    public JButton getVolumeBtn()    { return volumeBtn; }
    public JSlider getProgressBar()  { return progressBar; }
    public JSlider getVolumeSlider() { return volumeSlider; }
}