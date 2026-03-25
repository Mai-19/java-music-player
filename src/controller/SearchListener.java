package controller;

import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import view.View;

/**
 * SearchListener filters the song table as the user types in the search bar
 * 
 * it implements CaretListener which fires on every keystroke or cursor movement
 * in the text field - this is used instead of DocumentListener for simplicity
 * 
 * the filter is case-insensitive and matches against any column in the table
 * clearing the search field removes the filter and shows all songs again
 */
public class SearchListener implements CaretListener {

    private final View view;

    /**
     * creates the listener with a reference to the view
     * 
     * @param view the application view that owns the table sorter
     */
    public SearchListener(View view) {
        super();
        this.view = view;
    }

    /**
     * called whenever the cursor moves in the search field which happens on every keystroke
     * 
     * reads the current text from the field and applies a regex row filter
     * the filter uses a case-insensitive prefix so typing "rad" matches "Radiohead"
     * an empty search clears the filter and shows all songs
     * 
     * @param e the caret event from the text field
     */
    @Override
    public void caretUpdate(CaretEvent e) {
        String text = ((JTextField) e.getSource()).getText();
        // reset if search is empty (show all table rows)
        // otherwise apply filter and ignore capitalization
        if (text.isEmpty()) {
            view.setRowFilter(null);
        } else {
            view.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
}
