package wrburnham.kafkaproducer.gui.listener;

import wrburnham.kafkaproducer.gui.service.SaveChangesService;

public class SaveChangesOnCloseListener extends OnCloseWindowListener {

    public SaveChangesOnCloseListener(SaveChangesService service) {
        super(() -> {
            service.conditionalSaveAndExit();
            return null;
        });
    }
}
