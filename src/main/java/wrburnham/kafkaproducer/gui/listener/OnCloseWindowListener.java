package wrburnham.kafkaproducer.gui.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Supplier;

public abstract class OnCloseWindowListener implements WindowListener {

    private final Supplier<Void> action;

    public void doCloseAction() {
        action.get();
    }

    public OnCloseWindowListener(Supplier<Void> action) {
        this.action = action;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        doCloseAction();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
