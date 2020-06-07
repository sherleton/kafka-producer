package wrburnham.kafkaproducer.gui.support;

import wrburnham.kafkaproducer.model.KafkaProducerData;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

public interface DataAware {

    Optional<File> file();

    void conditionalExecute(Supplier<Boolean> condition, Supplier<Void> action);

    void reset();

    KafkaProducerData getDataFromUI();

    void setUI(KafkaProducerData data, File file);

    Optional<File> openDialog();

    void errorDialog(File file, String messageKey, Exception e);

    Optional<File> saveDialog();
}
