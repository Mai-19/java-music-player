import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

public class MusicPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private Model model;
    public MusicPanel(Model model) {
        super(new BorderLayout());
        setOpaque(false);
        this.model = model;

        String[] columns = {"Title", "Artist", "Album", "Year", "Length"};
        Object[][] data = {};

        tableModel = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        model.play (row);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 50, 50));
        super.paint(g2);

        // Reset clip so border isn't clipped, then draw on top
        g2.setClip(null);
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 50, 50));
        
        g2.dispose();
    }

    public void setData(Collection<Song> songs) { 
        tableModel.setRowCount(0);
        for (Song song : songs) { 
            tableModel.addRow(song.getInfo()); 
        } 
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
}