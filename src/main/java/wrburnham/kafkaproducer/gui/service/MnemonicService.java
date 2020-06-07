package wrburnham.kafkaproducer.gui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class MnemonicService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Character, Integer> mnemonics;

    public MnemonicService() {
        char min = 'A';
        char max = 'Z';
        logger.debug("Setting mnemonic map for chars from {} to {}", min, max);
        int capacity = max - min;
        mnemonics = new ConcurrentHashMap<>(capacity);
        for (char c = min; c <= max; c++) {
            mnemonics.put(c, 0);
        }
    }

    public Character get(String value) {
        TreeMap<Integer, Character> candidates = null;
        for (Character c : value.toCharArray()) {
            final Character ucaseChar = Character.toUpperCase(c);
            Integer count = mnemonics.get(ucaseChar);
            if (count == null) {
                continue;
            }
            if (count == 0) {
                mnemonics.put(ucaseChar, ++count);
                return ucaseChar;
            } else if (count > 0) {
                if (candidates == null) {
                    candidates = new TreeMap<>();
                }
                candidates.put(count, ucaseChar);
            }
        }
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException(
                    String.format(
                            "No mnemonic character found for %s",
                            value));
        }
        Map.Entry<Integer, Character> entry = candidates.firstEntry();
        mnemonics.put(entry.getValue(), entry.getKey()+1);
        return entry.getValue();
    }

}
