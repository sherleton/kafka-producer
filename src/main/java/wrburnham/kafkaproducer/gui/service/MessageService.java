package wrburnham.kafkaproducer.gui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ResourceBundle resourceBundle;

    private final Locale locale;

    public MessageService(Locale locale, String bundleBaseName) {
        logger.debug("Loading resource bundle {} for locale {}", bundleBaseName, locale);
        this.locale = locale;
        resourceBundle = ResourceBundle.getBundle(bundleBaseName, locale);
    }

    String get(String key) {
        return resourceBundle.getString(key);
    }

    String get(String key, Object...args) {
        return MessageFormat.format(get(key), args);
    }

    Locale locale() {
        return locale;
    }

}
