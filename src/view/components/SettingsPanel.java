package view.components;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import model.Model;
import view.Icons;
import view.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

public class SettingsPanel extends JPanel {

    private Model model;
    private View view;
    private JPanel directoryListPanel;
    private MusicPlayerButton backBtn, refreshBtn;

    public SettingsPanel(Model model, View view) {
        super();
        this.model = model;
        this.view = view;

        setLayout(new BorderLayout(0, 0));
        backBtn = new MusicPlayerButton(Icons.BACK);
        refreshBtn = new MusicPlayerButton(Icons.REFRESH);
        backBtn.setActionCommand("back");
        refreshBtn.setActionCommand("refresh");
        add(buildSettingsBox(), BorderLayout.CENTER);
    }

    private JPanel buildSettingsBox() {
        JPanel wrapper = new JPanel(new GridBagLayout());

        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(new RoundedBorder(20));
        box.setPreferredSize(new Dimension(550, 400));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 28f));
        titleRow.add(backBtn, BorderLayout.WEST);
        titleRow.add(title, BorderLayout.CENTER);
        titleRow.add(refreshBtn, BorderLayout.EAST);
        box.add(titleRow, BorderLayout.NORTH);

        box.add(buildDirectoriesTable(), BorderLayout.CENTER);

        wrapper.add(box);
        return wrapper;
    }
    
    private MusicPlayerButton addBtn;
    private JPanel buildDirectoriesTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(220, 220, 220));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel headerLabel = new JLabel("Directories");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));

        addBtn = new MusicPlayerButton(Icons.ADD_DIRECTORY);

        addBtn.setActionCommand("add directory");

        header.add(headerLabel, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Directory rows in a panel that stacks from the top
        directoryListPanel = new JPanel();
        directoryListPanel.setLayout(new BoxLayout(directoryListPanel, BoxLayout.Y_AXIS));

        refreshDirectoryList();

        // Wrap in a scroll pane so it doesn't overflow
        JScrollPane scroll = new JScrollPane(directoryListPanel);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void refreshDirectoryList() {
        directoryListPanel.removeAll();
        int i = 0;
        for (String string : model.getDirectories()) {
            directoryListPanel.add(buildDirectoryRow(string, i++));
        }
        // Filler pushes rows to the top instead of stretching them
        directoryListPanel.add(Box.createVerticalGlue());
        directoryListPanel.revalidate();
        directoryListPanel.repaint();
    }
    private MusicPlayerButton deleteBtn;
    private JPanel buildDirectoryRow(String path, int index) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Lock the row height to its preferred size so it never stretches
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        JLabel label = new JLabel(path);
        deleteBtn = new MusicPlayerButton(Icons.TRASH);
        deleteBtn.setActionCommand("remove directory:"+path);
        deleteBtn.addActionListener(view.getButtonListener());

        row.add(label, BorderLayout.WEST);
        row.add(deleteBtn, BorderLayout.EAST);
        return row;
    }

    public void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            model.addDirectory(chooser.getSelectedFile().getAbsolutePath());
            refreshDirectoryList();
        }
    }

    public JButton getBackBtn() { return backBtn; }
    public MusicPlayerButton getRefreshBtn() { return refreshBtn; }
    public MusicPlayerButton getAddDirectoryBtn() { return addBtn; }
    public MusicPlayerButton getDeleteBtn() { return deleteBtn; }
}