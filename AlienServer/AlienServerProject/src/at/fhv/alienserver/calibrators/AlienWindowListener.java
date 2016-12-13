package at.fhv.alienserver.calibrators;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by thomas on 13.12.16.
 */
public class AlienWindowListener implements WindowListener {
    AlienWindowListener(){/*super boring ctor*/}

    @Override
    public void windowOpened(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowClosing(WindowEvent e) {
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
