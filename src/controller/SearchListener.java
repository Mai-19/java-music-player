package controller;

import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import view.View;

/**
 * class for handling the search bar
 */
public class SearchListener implements CaretListener{

    private final View view;

    /**
     * constructor for the SearchListener class
     * @param view
     */
    public SearchListener(View view) {
        super();

        this.view = view;
    }

    /**
     * dynamically updates the table relative to whatever is typed into the search
     */
    @Override
    public void caretUpdate(CaretEvent e) {
        String text = ((JTextField)e.getSource()).getText();
        // reset if search is empty (show all table rows)
        // otherwise, apply filter and ignore capitalization
        if (text.isEmpty()) {
            view.setRowFilter(null);
        } else {
            view.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
    
}
