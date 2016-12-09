package at.fhv.alienserver.movingHead;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by thomas on 09.11.16.
 * Implements an extra minimal WindowListener. Used for nothing more than detecting that the input window is closed
 * by the user and trigger stopping of the app. Cheers.
 */
class AlienWindowListener implements WindowListener {
    AlienWindowListener(){/*super boring ctor*/}

    @Override
    public void windowOpened(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //System.out.println("Saving playing field dimensions in a super hacky binary file");
        //System.out.flush();
        //Calibrator.setRunning(false);
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
