package wrburnham.kafkaproducer;

import com.formdev.flatlaf.FlatLightLaf;
import wrburnham.kafkaproducer.gui.KafkaProducer;
import wrburnham.kafkaproducer.gui.factory.MenuItemFactory;
import wrburnham.kafkaproducer.gui.factory.TextContextMenuFactory;
import wrburnham.kafkaproducer.gui.service.DialogService;
import wrburnham.kafkaproducer.gui.service.LabelService;
import wrburnham.kafkaproducer.gui.service.MessageService;
import wrburnham.kafkaproducer.gui.service.MnemonicService;
import wrburnham.kafkaproducer.service.FileService;
import wrburnham.kafkaproducer.service.KafkaService;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class App {

    public static void main(String... args) {
        MessageService messageService = new MessageService(Locale.getDefault(), "messages");
        DialogService dialogService = new DialogService(messageService);
        FlatLightLaf.install();
        MenuItemFactory menuItemFactory = new MenuItemFactory();
        SwingUtilities.invokeLater(new KafkaProducer(
                new KafkaService(),
                new LabelService(
                        messageService,
                        new MnemonicService()),
                dialogService,
                new TextContextMenuFactory(
                        new LabelService(
                                messageService,
                                new MnemonicService()),
                        menuItemFactory,
                        Toolkit.getDefaultToolkit()),
                menuItemFactory,
                new FileService()).start());
    }

}
