import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class SettingsPanel extends JPanel {

    private Model model;
    private HashSet<String> directories;
    private JPanel directoryListPanel;
    private JButton backBtn;

    public SettingsPanel(Model model) {
        super();
        this.model = model;

        directories = new HashSet<>();

        setLayout(new BorderLayout(0, 0));
        backBtn = new JButton("back");
        backBtn.setActionCommand("back");
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
        titleRow.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);
        box.add(titleRow, BorderLayout.NORTH);

        box.add(buildDirectoriesTable(), BorderLayout.CENTER);

        wrapper.add(box);
        return wrapper;
    }

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
        JButton addBtn = new JButton("add");
        addBtn.addActionListener(e -> openFileChooser());

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

    private void refreshDirectoryList() {
        directoryListPanel.removeAll();
        int i = 0;
        for (String string : directories) {
            directoryListPanel.add(buildDirectoryRow(string, i++));
            
        }
        // Filler pushes rows to the top instead of stretching them
        directoryListPanel.add(Box.createVerticalGlue());
        directoryListPanel.revalidate();
        directoryListPanel.repaint();
    }

    private JPanel buildDirectoryRow(String path, int index) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Lock the row height to its preferred size so it never stretches
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        JLabel label = new JLabel(path);
        JButton deleteBtn = new JButton("delete");
        deleteBtn.addActionListener(e -> {
            directories.remove(path);
            model.removeDirectory(path);
            refreshDirectoryList();
        });

        row.add(label, BorderLayout.WEST);
        row.add(deleteBtn, BorderLayout.EAST);
        return row;
    }

    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            directories.add(chooser.getSelectedFile().getAbsolutePath());
            model.addDirectory(chooser.getSelectedFile().getAbsolutePath());
            refreshDirectoryList();
        }
    }

    public JButton getBackBtn() { return backBtn; }
    public HashSet<String> getDirectories() { return directories; }

    public void addDirectories(HashSet<String> d) {
        directories.addAll(d);
        refreshDirectoryList();
    }
}