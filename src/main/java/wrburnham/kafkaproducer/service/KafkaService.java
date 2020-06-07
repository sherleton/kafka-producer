package wrburnham.kafkaproducer.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.function.Function;

public class KafkaService {

    public void send(
            KafkaSendRequest request,
            Function<RecordMetadata, Void> success,
            Function<Exception, Void> error) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(request.getProperties());
        List<Header> headers = new ArrayList<>(request.getHeadersRaw().size());
        request.getHeadersRaw().forEach((k, v) -> headers.add(new RecordHeader(k, v.getBytes())));
        ProducerRecord<String, String> record = new ProducerRecord<>(
                request.getTopic(),
                request.getPartition(),
                request.getKey(),
                request.getValue(),
                headers);
        Future<RecordMetadata> result = producer.send(record, (recordMetadata, e) -> {
            if (e == null) {
                success.apply(recordMetadata);
            } else {
                error.apply(e);
            }
        });
    }

    public static class KafkaSendRequest {
        private final Properties properties;
        private final Map<String, String> headersRaw;
        private final String topic;
        private final int partition;
        private final String key;
        private final String value;

        public KafkaSendRequest(Properties properties, Map<String, String> headersRaw, String topic, int partition, String key, String value) {
            this.properties = properties;
            this.headersRaw = headersRaw;
            this.topic = topic;
            this.partition = partition;
            this.key = key;
            this.value = value;
        }

        public Properties getProperties() {
            return properties;
        }

        public Map<String, String> getHeadersRaw() {
            return headersRaw;
        }

        public String getTopic() {
            return topic;
        }

        public int getPartition() {
            return partition;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}