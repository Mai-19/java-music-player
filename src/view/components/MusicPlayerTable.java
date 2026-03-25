package view.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import view.View;

/**
 * MusicPlayerTable is a custom JTable styled for the music player
 *
 * it disables cell editing grid lines and column reordering
 * and applies alternating row background colors and themed header rendering
 */
public class MusicPlayerTable extends JTable {

    public MusicPlayerTable(DefaultTableModel model) {
        super(model);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(28);
        setFillsViewportHeight(true);
        setFont(getFont().deriveFont(13f));

        setShowGrid(false);
        setBackground(View.FOREGROUND);

        // style the header
        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
                label.setForeground(View.TEXT);
                label.setOpaque(true);
                label.setBackground(View.BACKGROUND);
                
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, View.ACCENT));
                return label;
            }
        });
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        if (c instanceof JComponent)
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());

        if (c instanceof JLabel)
            ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);

        if (isRowSelected(row)) {
            c.setBackground(View.ACCENT);
            c.setForeground(View.TEXT);
        } else {
            c.setBackground(row % 2 == 0 ? View.FOREGROUND : View.BACKGROUND);
            c.setForeground(View.TEXT);
        }
        return c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(View.FOREGROUND);
        for (int i = 0; i < getRowCount(); i++) {
            int y = (i + 1) * getRowHeight();
            g2.drawLine(0, y, getWidth(), y);
        }
        g2.dispose();
    }
}