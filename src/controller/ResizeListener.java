package controller;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import view.View;

/**
 * ResizeListener handles window resize events
 * 
 * when the user resizes the application window this listener
 * calls update on the view to repaint all components at the new size
 * this is needed because some custom painted components like
 * RoundedBorder and RoundedPanel do not automatically reflow
 */
public class ResizeListener extends ComponentAdapter {

    private final View view;

    /**
     * creates the listener with a reference to the view
     * 
     * @param view the application view to update on resize
     */
    public ResizeListener(View view) {
        super();
        this.view = view;
    }

    /**
     * called by Swing whenever the application window is resized
     * triggers a repaint of the entire view
     * 
     * @param e the component event from the resize
     */
    @Override
    public void componentResized(ComponentEvent e) {
        view.update();
    }
}
