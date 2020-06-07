package wrburnham.kafkaproducer.model;

import java.util.List;

public class KafkaProducerData {
    private final List<String> properties;
    private final List<Tuple> headers;
    private final String topic;
    private final String key;
    private final String partition;
    private final String message;

    public KafkaProducerData(List<String> properties, List<Tuple> headers, String topic, String key, String partition, String message) {
        this.properties = properties;
        this.headers = headers;
        this.topic = topic;
        this.key = key;
        this.partition = partition;
        this.message = message;
    }

    public List<String> properties() {
        return properties;
    }

    public List<Tuple> headers() {
        return headers;
    }

    public String topic() {
        return topic;
    }

    public String key() {
        return key;
    }

    public String partition() {
        return partition;
    }

    public String message() {
        return message;
    }
}