package view.components;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.TableMouseListener;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collection;

import model.Model;
import model.Song;
import view.View;

/**
 * Panel for displaying the music player
 */
public class MusicPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    /**
     * Constructor for the MusicPanel class <br>
     * this class contains the songs on a table
     * @param model
     * @param view
     */
    public MusicPanel(Model model, View view) {
        super(new BorderLayout());
        setOpaque(false);

        String[] columns = {"Title", "Artist", "Album", "Year", "Length"};
        Object[][] data = {};

        // create a model to hold the data
        tableModel = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new TableMouseListener(view, model));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        for (int i = 0; i < table.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 1));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Sets the data for the table
     * @param songs
     */
    public void setData(Collection<Song> songs) { 
        tableModel.setRowCount(0);
        for (Song song : songs) { 
            tableModel.addRow(song.getInfo()); 
        } 
    }

    /**
     * returns the row index of the given point in the table
     * @param point
     * @return
     */
    public int rowAtPoint(Point point) {
        return table.rowAtPoint(point);
    }

    /**
     * Converts a row index regardless of search into model index
     * @param row
     * @return
     */
    public int convertRowIndexToModel(int row) {
        return table.convertRowIndexToModel(row);
    }

    /**
     * for selecting the current playing song
     * @param row
     */
    public void setRowSelection(int row) {
        if (row >= table.getRowCount()) return;
        table.setRowSelectionInterval(row, row);
    }

    // getters
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getTableSorter() { return sorter; }
}