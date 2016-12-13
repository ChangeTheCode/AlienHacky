package at.fhv.alienserver.calibrators;

import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by thomas on 13.12.16.
 */
public class AlienKeyListener implements java.awt.event.KeyListener {
    private ArrayBlockingQueue<Character> KeyQueue;

    AlienKeyListener(ArrayBlockingQueue<Character> queue){
        KeyQueue = queue;
    }

    @Override
    public void keyPressed(KeyEvent e){
        char c = e.getKeyChar();
        if(c == 'w' || c == 'a' || c == 's' || c == 'd' || c == '1' || c == '2' || c == '3' || c == '4' || c == 'r') {
            KeyQueue.add(c);
        }
    }

    @Override
    public void keyReleased(KeyEvent e){
        //don't care
    }

    @Override
    public void keyTyped(KeyEvent e){
        //care even less
    }
}