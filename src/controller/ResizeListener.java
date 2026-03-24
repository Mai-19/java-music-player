package controller;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import view.View;

/**
 * Class for handling the resizing of the application <br>
 * repaints borders and components
 */
public class ResizeListener extends ComponentAdapter{
    private final View view;
    /**
     * constructor for the ResizeListener class
     * @param view
     */
    public ResizeListener(View view) {
        super();
        this.view = view;
    }

    /**
     * Called when the application is resized <br>
     * updates view
     */
    @Override
    public void componentResized(ComponentEvent e) {
        view.update();
    }
}
