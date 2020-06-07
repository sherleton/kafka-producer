package wrburnham.kafkaproducer.gui.support;

import wrburnham.kafkaproducer.gui.service.LabelService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Status  implements DocumentListener, TableModelListener {

    private final LabelService labelService;

    private final JFrame window;

    public Status(LabelService labelService, JFrame window) {
        this.labelService = labelService;
        this.window = window;
    }

    public void init() {
        this.file = null;
        setTitle();
        reset();
    }

    public void update(File file) {
        this.file = file;
        setTitle();
        reset();
    }

    private File file;

    public Optional<File> file() {
        return file == null ? Optional.empty() : Optional.of(file);
    }

    private void setTitle() {
        window.setTitle(String.format("%s - %s",
                labelService.getText("title"),
                file().isPresent() ?
                        file.getName() :
                        labelService.getText("app.file-untitled")));
    }

    private AtomicBoolean changed = new AtomicBoolean(false);

    private void reset() {
        this.changed.set(false);
    }

    public boolean hasChanges() {
        return changed.get();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed.set(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed.set(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed.set(true);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        changed.set(true);
    }

}
