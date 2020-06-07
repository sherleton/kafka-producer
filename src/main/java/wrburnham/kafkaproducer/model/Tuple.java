package wrburnham.kafkaproducer.model;

public class Tuple {
        private final String key;
        private final String value;

        public Tuple(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }