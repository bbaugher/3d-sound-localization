/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone3daudiodemo;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 *
 * @author Ian
 * This simple class simply implements the LineListener
 * interface and comes with a boolean so we can check
 * for when the audio file finishes playing
 */
public class LineListenerEnd implements LineListener {
    public boolean finished=false;
    
    public void update(LineEvent event)
    {
        if (event.getType() == LineEvent.Type.STOP)
        {
            finished = true;
            event.getLine().close();
        }
    }
    
}
