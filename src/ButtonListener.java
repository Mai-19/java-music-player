import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonListener implements ActionListener{

    private Model model;
    public ButtonListener(Model model) {
        super();
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "settings":
                model.changeView(Cards.SETTINGS);
                break;
            case "back":
                model.changeView(Cards.PLAYER);
                break;
            default:
                break;
        }
    }
}
