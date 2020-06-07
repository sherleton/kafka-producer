package wrburnham.kafkaproducer.gui.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import wrburnham.kafkaproducer.gui.KafkaProducer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Future;

public class DialogService {

    private final MessageService messageService;

    public DialogService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void error(Component parent, String messageKey, Object... args) {
        JOptionPane.showMessageDialog(
                parent,
                messageService.get(messageKey, args),
                messageService.get("dialog.error"),
                JOptionPane.ERROR_MESSAGE);
    }

    public boolean yesNo(Component parent, String messageKey, Object... args) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                messageService.get(messageKey, args),
                messageService.get("dialog.warning"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        return JOptionPane.YES_OPTION == result;
    }

    public void info(Component parent, String messageKey, Object... args) {
        JOptionPane.showMessageDialog(
                parent,
                messageService.get(messageKey, args),
                messageService.get("dialog.info"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public int yesNoCancel(Component parent, String messageKey, Object... args) {
        return JOptionPane.showConfirmDialog(
                parent,
                messageService.get(messageKey, args),
                messageService.get("dialog.warning"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    public Optional<File> open(Component parent, String titleKey) {
        return fileChooser(parent, titleKey, FileChooserType.OPEN);
    }

    public Optional<File> save(Component parent) {
        return fileChooser(parent, "dialog.save-as", FileChooserType.SAVE);
    }

    private Optional<File> fileChooser(Component parent, String titleKey, FileChooserType type) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setLocale(messageService.locale());
        fileChooser.setDialogTitle(messageService.get(titleKey));
        final String xmlSuffix = ".xml";
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return !f.isDirectory() &&
                        f.getName().endsWith(xmlSuffix);
            }
            @Override
            public String getDescription() {
                return messageService.get("app.file-type-description.xml");
            }
        });
        fileChooser.setAcceptAllFileFilterUsed(false);
        final int selectedOption;
        if (FileChooserType.SAVE.equals(type)) {
            selectedOption = fileChooser.showSaveDialog(parent);
        } else if (FileChooserType.OPEN.equals(type)) {
            selectedOption = fileChooser.showOpenDialog(parent);
        } else {
            throw new IllegalStateException(String.format("Type %s not valid", type));
        }
        final Optional<File> result;
        if (JFileChooser.APPROVE_OPTION == selectedOption) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().endsWith(xmlSuffix)) {
                file = new File(file + xmlSuffix);
            }
            result = Optional.of(file);
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public ProgressMonitor progress(Component parent, String messageKey) {
        return new ProgressMonitor(
                parent,
                messageService.get(messageKey),
                null,
                0,
                100);
    }

    private enum FileChooserType {
        OPEN, SAVE
    }

}
