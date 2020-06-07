package wrburnham.kafkaproducer.gui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MessageService messageService;

    private final MnemonicService mnemonicService;

    public LabelService(MessageService messageService, MnemonicService mnemonicService) {
        this.messageService = messageService;
        this.mnemonicService = mnemonicService;
    }

    public GUILabel getLabel(String key) {
        String text = getText(key);
        char mnemonic = mnemonicService.get(text);
        return new GUILabel() {
            @Override
            public String text() {
                return text;
            }
            @Override
            public char mnemonic() {
                return mnemonic;
            }
        };
    }

    public String getText(String key) {
        return messageService.get(key);
    }

    String getText(String key, Object... args) {
        return messageService.get(key, args);
    }

}
