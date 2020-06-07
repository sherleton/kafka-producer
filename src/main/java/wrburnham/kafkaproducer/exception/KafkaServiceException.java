package wrburnham.kafkaproducer.exception;

public class KafkaServiceException extends Exception {
    public KafkaServiceException(Exception e) {
        super(e);
    }
}
