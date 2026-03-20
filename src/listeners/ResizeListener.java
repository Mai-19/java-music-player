package listeners;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import view.View;

public class ResizeListener extends ComponentAdapter{
    private View view;
    public ResizeListener(View view) {
        super();
        this.view = view;
    }
    @Override
    public void componentResized(ComponentEvent e) {
        view.update();
    }
}
