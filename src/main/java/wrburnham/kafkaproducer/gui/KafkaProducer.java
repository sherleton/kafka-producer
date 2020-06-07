package wrburnham.kafkaproducer.gui;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import wrburnham.kafkaproducer.gui.factory.MenuItemFactory;
import wrburnham.kafkaproducer.gui.factory.TextContextMenuFactory;
import wrburnham.kafkaproducer.gui.listener.SaveChangesOnCloseListener;
import wrburnham.kafkaproducer.gui.service.DialogService;
import wrburnham.kafkaproducer.gui.service.GUILabel;
import wrburnham.kafkaproducer.gui.service.LabelService;
import wrburnham.kafkaproducer.gui.service.SaveChangesService;
import wrburnham.kafkaproducer.gui.support.DataAware;
import wrburnham.kafkaproducer.gui.support.KafkaWorker;
import wrburnham.kafkaproducer.gui.support.Status;
import wrburnham.kafkaproducer.model.KafkaProducerData;
import wrburnham.kafkaproducer.model.Tuple;
import wrburnham.kafkaproducer.service.FileService;
import wrburnham.kafkaproducer.service.KafkaService;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KafkaProducer extends JFrame implements DataAware {

    private final LabelService labelService;

    private final DialogService dialogService;

    private final TextContextMenuFactory textContextMenuFactory;

    private final String defaultPartitionText = "0";

    private final String defaultPropertiesText = new PropBuilder()
            .prop(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
            .prop(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
            .prop(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
            .toString();

    private final String empty = "";

    private final JTextArea textAreaProperties;

    private final JScrollPane scrollPaneTextArea;

    private final JLabel labelProperties;

    private final JTable tableHeaders;

    private final JLabel labelHeaders;

    private final JScrollPane scrollPaneHeaders;

    private final JToolBar headersToolbar;

    private final JTextField textTopic;

    private final JLabel labelTopic;

    private final JTextField textKey;

    private final JLabel labelKey;

    private final JTextField textPartition;

    private final JLabel labelPartition;

    private final JTextArea textAreaMessage;

    private final JScrollPane scrollPaneTextAreaMessage;

    private final JLabel labelMessage;

    private final JButton buttonSendMessage;

    private final JMenuBar menuBar;

    private final Status status;

    private final SaveChangesOnCloseListener windowListener;

    public KafkaProducer(KafkaService kafkaService,
                         LabelService labelService,
                         DialogService dialogService,
                         TextContextMenuFactory textContextMenuFactory,
                         MenuItemFactory menuItemFactory,
                         FileService fileService) {
        this.labelService = labelService;
        this.dialogService = dialogService;
        this.textContextMenuFactory = textContextMenuFactory;
        status = new Status(labelService, this);
        SaveChangesService saveChangesService = new SaveChangesService(
                this,
                fileService);
        windowListener = new SaveChangesOnCloseListener(saveChangesService);
        textAreaProperties = textArea(
                defaultPropertiesText,
                status);
        textAreaProperties.setRows(4);
        scrollPaneTextArea = scrollPane(textAreaProperties);
        labelProperties = label("properties", scrollPaneTextArea);
        tableHeaders = tableHeaders();
        labelHeaders = label("kafka.headers", tableHeaders);
        scrollPaneHeaders = scrollPane(tableHeaders);
        int scrollPaneHeadersPrefHeight = tableHeaders.getRowHeight() * 5 + tableHeaders.getTableHeader().getHeight();
        scrollPaneHeaders.setPreferredSize(
                new Dimension(
                        scrollPaneHeaders.getPreferredSize().width,
                        scrollPaneHeadersPrefHeight));
        JButton buttonAddHeader = button(
                "kafka.add-header",
                e -> {
                    DefaultTableModel model = ((DefaultTableModel) tableHeaders.getModel());
                    final boolean canAdd;
                    if (model.getRowCount() == 0) {
                        canAdd = true;
                    } else {
                        String lastRowKey = (String) model.getValueAt(model.getRowCount() - 1, 0);
                        canAdd = lastRowKey != null && !empty.equals(lastRowKey.trim());
                    }
                    if (canAdd) {
                        model.addRow(new String[]{empty, empty});
                        tableHeaders.editCellAt(model.getRowCount() - 1, 0);
                        tableHeaders.getEditorComponent().requestFocus();
                    } else {
                        dialogService.error(
                                this,
                                "kafka.error.please-provide-a-key-for-row-number",
                                model.getRowCount());
                    }
                });
        JButton buttonDeleteHeader = button(
                "kafka.remove-header",
                e -> {
                    DefaultTableModel model = (DefaultTableModel) tableHeaders.getModel();
                    int[] rows = tableHeaders.getSelectedRows();
                    for (int i = rows.length - 1; i >= 0; i--) {
                        model.removeRow(rows[i]);
                    }
                });
        headersToolbar = toolbar();
        headersToolbar.add(buttonAddHeader);
        headersToolbar.add(buttonDeleteHeader);
        textTopic = textField(empty, status);
        textContextMenuFactory.textContextMenu(textTopic);
        labelTopic = label("kafka.topic", textTopic);
        textKey = textField(empty, status);
        textContextMenuFactory.textContextMenu(textKey);
        labelKey = label("kafka.key", textKey);
        textPartition = textField(defaultPartitionText, status);
        textContextMenuFactory.textContextMenu(textPartition);
        labelPartition = label("kafka.partition", textPartition);
        textAreaMessage = textArea(empty, status);
        textAreaMessage.setRows(8);
        scrollPaneTextAreaMessage = new JScrollPane(textAreaMessage);
        scrollPaneTextAreaMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelMessage = label("kafka.message", scrollPaneTextAreaMessage);
        buttonSendMessage = button(
                "kafka.send",
                e -> {
                    final Properties properties;
                    try (StringReader sr = new StringReader(textAreaProperties.getText())) {
                        properties = new Properties();
                        properties.load(sr);
                    } catch (IOException ex) {
                        dialogService.error(
                                this,
                                "kafka.error.properties", ex.getMessage());
                        return;
                    }
                    if (empty(properties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))) {
                        dialogService.error(this, "kafka.error.properties-key-not-set", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
                        return;
                    }
                    if (empty(properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))) {
                        dialogService.error(this, "kafka.error.properties-key-not-set", ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
                        return;
                    }
                    if (empty(properties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))) {
                        dialogService.error(this, "kafka.error.properties-key-not-set", ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
                        return;
                    }
                    final String topic = textTopic.getText();
                    if (empty(topic)) {
                        dialogService.error(this, "kafka.error.field-empty", labelTopic.getText());
                        return;
                    }

                    final String partitionRaw = textPartition.getText();
                    if (empty(partitionRaw)) {
                        dialogService.error(this, "kafka.error.field-empty", labelPartition.getText());
                        return;
                    }
                    final int partition;
                    try {
                        partition = Integer.parseInt(partitionRaw);
                    } catch (NumberFormatException ex) {
                        dialogService.error(this, "kafka.error.field-value-not-valid", labelPartition.getText(), partitionRaw);
                        return;
                    }

                    final String value = textAreaMessage.getText();
                    if (empty(value)) {
                        dialogService.error(this, "kafka.error.field-value-not-valid", labelMessage.getText(), empty);
                        return;
                    }

                    TableModel model = tableHeaders.getModel();
                    final Map<String, String> headers = new HashMap<>(model.getRowCount());
                    for (int i = 0; i < model.getRowCount(); i++) {
                        headers.put(
                                (String) model.getValueAt(i, 0),
                                (String) model.getValueAt(i, 1));
                    }
                    if (headers.isEmpty()) {
                        if (!dialogService.yesNo(this, "kafka.warning.prompt.field-empty-continue", labelHeaders.getText())) {
                            return;
                        }
                    }
                    final String key = textKey.getText();
                    if (empty(key)) {
                        if (!dialogService.yesNo(
                                this,
                                "kafka.error.field-empty-use-default",
                                labelKey.getText(),
                                "null")) {
                            dialogService.error(this, "kafka.error.send-message-abort");
                            return;
                        }
                    }
                    new KafkaWorker(
                            kafkaService,
                            dialogService,
                            new KafkaService.KafkaSendRequest(
                                    properties,
                                    headers,
                                    topic,
                                    partition,
                                    key,
                                    value), this).execute();
                    dialogService.info(this, "kafka.info.send-message-success");
                });
        menuBar = new JMenuBar();
        menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JMenu fileMenu = new JMenu();
        fileMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        GUILabel fileLabel = labelService.getLabel("menu.file");
        fileMenu.setText(fileLabel.text());
        fileMenu.setMnemonic(fileLabel.mnemonic());
        fileMenu.add(menuItemFactory.enabledItem(
                labelService.getLabel("menu.file.new"),
                e -> saveChangesService.conditionalSaveAndReset(),
                KeyEvent.VK_N
        ));
        fileMenu.add(menuItemFactory.enabledItem(
                labelService.getLabel("menu.file.open"),
                e -> saveChangesService.conditionalSaveAndOpen(),
                KeyEvent.VK_O
        ));
        fileMenu.add(menuItemFactory.enabledItem(
                labelService.getLabel("menu.file.save"),
                e -> saveChangesService.runSaveRoutine(),
                KeyEvent.VK_S
        ));
        fileMenu.add(menuItemFactory.enabledItem(
                labelService.getLabel("menu.file.save-as"),
                e -> saveChangesService.runSaveAsRoutine(),
                KeyEvent.VK_A
        ));
        fileMenu.addSeparator();
        fileMenu.add(menuItemFactory.enabledItem(
                labelService.getLabel("menu.file.exit"),
                e -> windowListener.doCloseAction(),
                KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK
        ));
        menuBar.add(fileMenu);
    }

    private JToolBar toolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.setFloatable(false);
        return toolBar;
    }

    private JTable tableHeaders() {
        JTable tableHeaders = new JTable(
                new DefaultTableModel(
                        new String[][]{},
                        new String[]{
                                labelService.getText("kafka.header-key"),
                                labelService.getText("kafka.header-value")}));
        tableHeaders.putClientProperty("terminateEditOnFocusLost", true);
        tableHeaders.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableHeaders.getModel().addTableModelListener(status);
        return tableHeaders;
    }

    private JScrollPane scrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scrollPane;
    }

    @Override
    public void setUI(KafkaProducerData data, File file) {
        textAreaProperties.setText(data.properties()
                .stream()
                .map(this::unescape)
                .collect(Collectors.joining(System.lineSeparator())));
        deleteRows();
        DefaultTableModel model = (DefaultTableModel) tableHeaders.getModel();
        data.headers().forEach(h -> model.addRow(
                new Object[]{
                        unescape(h.key()),
                        unescape(h.value())
                }));
        textTopic.setText(unescape(data.topic()));
        textPartition.setText(unescape(data.partition()));
        textKey.setText(unescape(data.key()));
        textAreaMessage.setText(unescape(data.message()));
        status.update(file);
    }

    public Runnable start() {
        status.init();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(windowListener);

        JPanel menuPanel = yPanel();
        JPanel mainPanel = yPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel tkpPanel = xPanel();
        JPanel topicPanel = yPanel();
        topicPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JPanel keyPanel = yPanel();
        keyPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        JPanel partitionPanel = yPanel();
        partitionPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        JPanel buttonPanel = xPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        menuPanel.add(menuBar);

        buttonPanel.add(buttonSendMessage);

        topicPanel.add(labelTopic);
        topicPanel.add(textTopic);
        keyPanel.add(labelKey);
        keyPanel.add(textKey);
        partitionPanel.add(labelPartition);
        partitionPanel.add(textPartition);

        tkpPanel.add(topicPanel);
        tkpPanel.add(keyPanel);
        tkpPanel.add(partitionPanel);

        mainPanel.add(labelProperties);
        mainPanel.add(scrollPaneTextArea);
        mainPanel.add(labelHeaders);
        mainPanel.add(scrollPaneHeaders);
        mainPanel.add(headersToolbar);
        mainPanel.add(tkpPanel);
        mainPanel.add(labelMessage);
        mainPanel.add(scrollPaneTextAreaMessage);
        mainPanel.add(buttonPanel);

        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(menuPanel);
        add(mainPanel);

        return () -> {
            this.pack();
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        };
    }

    @Override
    public KafkaProducerData getDataFromUI() {
        Vector<Vector> headers = ((DefaultTableModel) tableHeaders.getModel()).getDataVector();
        return new KafkaProducerData(
                Arrays.stream(textAreaProperties.getText()
                        .split("\r?\n"))
                        .map(this::escape)
                        .collect(Collectors.toList()),
                headers.stream()
                        .map(v -> new Tuple(
                                escape(v.get(0).toString()),
                                escape(v.get(1).toString())))
                        .collect(Collectors.toList()),
                escape(textTopic.getText()),
                escape(textKey.getText()),
                escape(textPartition.getText()),
                escape(textAreaMessage.getText()));
    }

    @Override
    public Optional<File> openDialog() {
        return dialogService.open(
                this,
                "dialog.open");
    }

    @Override
    public void errorDialog(File file, String messageKey, Exception e) {
        dialogService.error(
                this,
                messageKey,
                file.getName(),
                e.getMessage());
    }

    @Override
    public Optional<File> saveDialog() {
        return dialogService.save(this);
    }

    @Override
    public void conditionalExecute(Supplier<Boolean> condition, Supplier<Void> action) {
        if (!status.hasChanges()) {
            action.get();
        } else {
            final int result = dialogService.yesNoCancel(this, "app.save-changes");
            switch (result) {
                case JOptionPane.YES_OPTION:
                    if (condition.get()) {
                        action.get();
                    }
                    break;
                case JOptionPane.NO_OPTION:
                    action.get();
                    break;
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    break;
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "Expected yes, no cancel or close but result was %d",
                                    result));
            }
        }
    }

    private String escape(String s) {
        return StringEscapeUtils.escapeXml11(s);
    }

    private String unescape(String s) {
        return StringEscapeUtils.unescapeXml(s);
    }

    @Override
    public void reset() {
        textAreaProperties.setText(defaultPropertiesText);
        textTopic.setText(empty);
        textKey.setText(empty);
        textPartition.setText(defaultPartitionText);
        textAreaMessage.setText(empty);
        deleteRows();
        status.init();
    }

    private void deleteRows() {
        DefaultTableModel model = (DefaultTableModel) tableHeaders.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }

    private JTextArea textArea(String defaultText, DocumentListener documentListener) {
        JTextArea textArea = new JTextArea(defaultText);
        textContextMenuFactory.textContextMenu(textArea);
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        textArea.getDocument().addDocumentListener(documentListener);
        return textArea;
    }

    private JPanel xPanel() {
        JPanel xPanel = new JPanel();
        xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.X_AXIS));
        xPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return xPanel;
    }

    private JPanel yPanel() {
        JPanel yPanel = new JPanel();
        yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.Y_AXIS));
        yPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return yPanel;
    }

    private JButton button(String resourceKey, ActionListener listener) {
        GUILabel label = labelService.getLabel(resourceKey);
        JButton button = new JButton(label.text());
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMnemonic(label.mnemonic());
        button.addActionListener(listener);
        return button;
    }

    private JLabel label(String resourceKey, JComponent labelled) {
        GUILabel guiLabel = labelService.getLabel(resourceKey);
        JLabel label = new JLabel(guiLabel.text());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLabelFor(labelled);
        label.setDisplayedMnemonic(guiLabel.mnemonic());
        return label;
    }

    private JTextField textField(String defaultText, DocumentListener documentListener) {
        JTextField field = new JTextField(defaultText);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(
                field.getMaximumSize().width,
                field.getPreferredSize().height
        ));
        field.getDocument().addDocumentListener(documentListener);
        return field;
    }

    private boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class PropBuilder {
        private final StringBuilder sb;

        private PropBuilder() {
            this.sb = new StringBuilder();
        }

        PropBuilder prop(String key, String value) {
            sb.append(System.lineSeparator());
            sb.append(key);
            sb.append("=");
            sb.append(value);
            return this;
        }

        @Override
        public String toString() {
            return sb.substring(System.lineSeparator().length());
        }
    }

    @Override
    public Optional<File> file() {
        return status.file();
    }

}
