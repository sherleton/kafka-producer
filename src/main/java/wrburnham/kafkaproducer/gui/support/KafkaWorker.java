package wrburnham.kafkaproducer.gui.support;

import wrburnham.kafkaproducer.gui.service.DialogService;
import wrburnham.kafkaproducer.service.KafkaService;

import javax.swing.*;
import java.awt.*;

public class KafkaWorker extends SwingWorker<Void, Void> {

    private final KafkaService kafkaService;

    private final DialogService dialogService;

    private final KafkaService.KafkaSendRequest request;

    private final Component parent;

    public KafkaWorker(KafkaService kafkaService, DialogService dialogService, KafkaService.KafkaSendRequest request, Component parent) {
        this.kafkaService = kafkaService;
        this.dialogService = dialogService;
        this.request = request;
        this.parent = parent;
    }

    @Override
    protected Void doInBackground() throws Exception {
        kafkaService.send(request, rm -> {
                dialogService.info(parent, "kafka.info.send-message-success");
                return null;
            },
            e -> {
                dialogService.error(parent, "kafka.error.send-message", e.getMessage());
                return null;
            });
        return null;
    }

}
