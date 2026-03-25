package model;

import net.beadsproject.beads.core.Bead;

/**
 * SongEndListener listens for when the current song finishes playing
 * 
 * it extends Bead which is the base class for all audio event listeners
 * in the Beads audio library
 * 
 * when the SamplePlayer reaches the end of a file it sends a message
 * to its kill listener - that kill listener is this class
 * when the message arrives nextSong is called on the model
 * so playback automatically continues to the next track
 */
public class SongEndListener extends Bead {
    
    // reference to the model so we can trigger the next song
    private final Model model;

    /**
     * creates the listener and stores a reference to the model
     * 
     * @param model the main application model
     */
    public SongEndListener(Model model) {
        super();
        this.model = model;
    }

    /**
     * called automatically by the Beads audio engine when the
     * SamplePlayer finishes playing the current song
     * 
     * triggers nextSong on the model which advances the queue
     * and starts playing the next track
     * 
     * @param message the bead message sent by the audio engine - not used directly
     */
    @Override
    protected void messageReceived(Bead message) {
        super.messageReceived(message);
        model.nextSong();
    }
}
