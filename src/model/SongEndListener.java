package model;

import net.beadsproject.beads.core.Bead;

public class SongEndListener extends Bead {
    
    private Model model;

    public SongEndListener(Model model) {
        super();
        this.model = model;
    }

    @Override
    protected void messageReceived(Bead message) {
        super.messageReceived(message);
        model.nextSong();
    }
}
