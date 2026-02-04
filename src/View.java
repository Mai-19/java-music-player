import javax.swing.JFrame;

public class View {
    private Model model;

    public View(Model model) {
        super();

        this.model = model;
        this.model.addGUI(this);
        
        createFrame();
        registerControllers();
        update();
    }

    private JFrame frame;
    private void createFrame() {
        frame = new JFrame();

        addComponents();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 540); // half of 1920x1080. TODO: swap to some more reliable form of getting screen size?
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addComponents() {
        
    }

    private void registerControllers() {
        
    }

    public void update() {
        frame.repaint();
    }
}
