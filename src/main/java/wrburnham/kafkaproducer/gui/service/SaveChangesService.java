package wrburnham.kafkaproducer.gui.service;

import wrburnham.kafkaproducer.gui.support.DataAware;
import wrburnham.kafkaproducer.model.KafkaProducerData;
import wrburnham.kafkaproducer.service.FileService;

import java.io.File;
import java.util.Optional;

public class SaveChangesService {

    private final DataAware dataAware;

    private final FileService fileService;

    public SaveChangesService(
            DataAware dataAware,
            FileService fileService) {
        this.dataAware = dataAware;
        this.fileService = fileService;
    }

    public void conditionalSaveAndReset() {
        dataAware.conditionalExecute(
                this::runSaveRoutine,
                () -> {
                    dataAware.reset();
                    return null;
                }
        );
    }

    public void conditionalSaveAndOpen() {
        dataAware.conditionalExecute(
                this::runSaveRoutine,
                () -> {
                    runOpenRoutine();
                    return null;
                }
        );
    }

    public void conditionalSaveAndExit() {
        dataAware.conditionalExecute(
                this::runSaveRoutine,
                () -> {
                    System.exit(0);
                    return null;
                }
        );
    }

    public boolean runSaveRoutine() {
        if (!dataAware.file().isPresent()) {
            return runSaveAsRoutine();
        } else {
            return save(dataAware.getDataFromUI(),
                    dataAware.file().get());
        }
    }

    /**
     * @return true if save was performed, false otherwise.
     */
    public boolean runSaveAsRoutine() {
        return dataAware.saveDialog()
                .filter(file -> save(
                        dataAware.getDataFromUI(),
                        file)).isPresent();
    }

    /**
     * Save a {@link KafkaProducerData} to disk and update
     * the UI.
     * If an exception occurs, an error message is shown and
     * false is returned.
     *
     * @param data the data to save.
     * @param file where to save it.
     * @return true if data was saved, false otherwise.
     */
    private boolean save(KafkaProducerData data, File file) {
        try {
            fileService.save(data, file);
            dataAware.setUI(data, file);
            return true;
        } catch (Exception ex) {
            dataAware.errorDialog(file, "app.save-error", ex);
            return false;
        }
    }

    private void runOpenRoutine() {
        Optional<File> result = dataAware.openDialog();
        if (result.isPresent()) {
            final File file = result.get();
            KafkaProducerData data = null;
            try {
                data = fileService.open(file);
            } catch (Exception ex) {
                dataAware.errorDialog(file, "app.open-file-not-valid", ex);
            }
            if (data != null) {
                dataAware.setUI(data, file);
            }
        }
    }
}
