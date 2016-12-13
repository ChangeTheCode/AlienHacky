package at.fhv.alienserver.calibrators;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by thomas on 13.12.16.
 */
public class AlienWindowListener implements WindowListener {
    AlienWindowListener(){
        /*super boring ctor*/
    }

    @Override
    public void windowOpened(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowClosing(WindowEvent e) {
        /*
         * Fixme: replace this hardcoded bullshit
         * To do so, one could implement an interface that defines this method
         * and then supply this listener with an instance of that interface.
         * This however requires that the calibrators have no static context, so
         * they can not contain the main. Rather they had to be instantiated in it.
         */
        PlayingAreaCalibrator.setRunning(false);
        MHCalibrator.setRunning(false);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowIconified(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        //don't care
    }
}
