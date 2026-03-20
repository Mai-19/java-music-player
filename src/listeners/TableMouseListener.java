package listeners;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import model.Model;
import view.View;

public class TableMouseListener extends MouseAdapter{

    private View view;
    private Model model;

    public TableMouseListener(View view, Model model) {
        super();
        this.view = view;
        this.model = model;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = view.rowAtPoint(e.getPoint());
            if (row != -1) {
                model.play(view.convertRowIndexToModel(row));
            }
        }
        view.setPlayback("pause");
    }
}
