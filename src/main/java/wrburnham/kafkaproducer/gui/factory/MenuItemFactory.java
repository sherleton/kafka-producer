package wrburnham.kafkaproducer.gui.factory;

import wrburnham.kafkaproducer.gui.service.GUILabel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class MenuItemFactory {

    public JMenuItem enabledItem(GUILabel label, ActionListener listener, int keyEvent, int modifiers) {
        JMenuItem item = item(label, listener);
        item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifiers));
        return item;
    }

    public JMenuItem enabledItem(GUILabel label, ActionListener listener, int keyEvent) {
        JMenuItem item = item(label, listener);
        item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, KeyEvent.CTRL_DOWN_MASK));
        return item;
    }

    public JMenuItem disabledItem(GUILabel label, ActionListener listener, int keyEvent) {
        JMenuItem item = enabledItem(label, listener, keyEvent);
        item.setEnabled(false);
        return item;
    }

    private JMenuItem item(GUILabel label, ActionListener listener) {
        JMenuItem item = new JMenuItem(label.text(), label.mnemonic());
        item.addActionListener(listener);
        return item;
    }

}
