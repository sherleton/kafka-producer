package wrburnham.kafkaproducer.gui.factory;

import wrburnham.kafkaproducer.gui.service.GUILabel;
import wrburnham.kafkaproducer.gui.service.LabelService;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TextContextMenuFactory {

    private final Clipboard clipboard;

    private final MenuItemFactory menuItemFactory;

    private final GUILabel cut;
    private final GUILabel copy;
    private final GUILabel paste;
    private final GUILabel delete;
    private final GUILabel selectAll;

    private static class TextContextMenuItems {
        private final JMenuItem cut;
        private final JMenuItem copy;
        private final JMenuItem paste;
        private final JMenuItem delete;
        private final JMenuItem selectAll;

        private TextContextMenuItems(JMenuItem cut, JMenuItem copy, JMenuItem paste, JMenuItem delete, JMenuItem selectAll) {
            this.cut = cut;
            this.copy = copy;
            this.paste = paste;
            this.delete = delete;
            this.selectAll = selectAll;
        }
    }

    public TextContextMenuFactory(LabelService labelService, MenuItemFactory menuItemFactory, Toolkit toolkit) {
        this.clipboard = toolkit.getSystemClipboard();
        this.menuItemFactory = menuItemFactory;
        cut = labelService.getLabel("menu.cut");
        copy = labelService.getLabel("menu.copy");
        paste = labelService.getLabel("menu.paste");
        delete = labelService.getLabel("menu.delete");
        selectAll = labelService.getLabel("menu.select-all");
    }

    public void textContextMenu(JTextComponent component) {
        TextContextMenuItems textContextMenuItems = new TextContextMenuItems(
                menuItemFactory.disabledItem(cut, e -> component.cut(), KeyEvent.VK_X),
                menuItemFactory.disabledItem(copy, e -> component.copy(), KeyEvent.VK_C),
                menuItemFactory.disabledItem(paste, e -> component.paste(), KeyEvent.VK_V),
                menuItemFactory.disabledItem(delete, e -> component.replaceSelection(""), KeyEvent.VK_DELETE),
                menuItemFactory.disabledItem(selectAll, e -> component.selectAll(), KeyEvent.VK_A));
        JPopupMenu menu = new JPopupMenu();
        menu.add(textContextMenuItems.cut);
        menu.add(textContextMenuItems.copy);
        menu.add(textContextMenuItems.paste);
        menu.add(textContextMenuItems.delete);
        menu.add(new JSeparator());
        menu.add(textContextMenuItems.selectAll);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e, menu, textContextMenuItems, component);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e, menu, textContextMenuItems, component);
            }
        });
    }

    private void handleMouseEvent(MouseEvent e, JPopupMenu menu, TextContextMenuItems textContextMenuItems, JTextComponent component) {
        if (!component.isEnabled() || MouseEvent.BUTTON3 != e.getButton()) {
            return;
        }
        component.requestFocus();
        textContextMenuItems.selectAll.setEnabled(notEmpty(component.getText()));
        boolean selectedText = notEmpty(component.getSelectedText());
        textContextMenuItems.copy.setEnabled(selectedText);
        if (component.isEditable()) {
            textContextMenuItems.cut.setEnabled(selectedText);
            textContextMenuItems.delete.setEnabled(selectedText);
            textContextMenuItems.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor));
        }
        menu.show(component, e.getX(), e.getY());
    }

    private boolean notEmpty(String value) {
        return value != null && value.length() != 0;
    }

}
