package at.fhv.alienserver.calibrators;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by thomas on 13.12.16. Differs from the standard window listener by the setRunning() function of its
 * owning parent to signal it to stop, when the window gets closed.
 */
class AlienWindowListener implements WindowListener {
    private ICalibrator parent;

    AlienWindowListener(ICalibrator parent){
        this.parent = parent;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        //don't care
    }

    @Override
    public void windowClosing(WindowEvent e) {
        parent.setRunning(false);
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
