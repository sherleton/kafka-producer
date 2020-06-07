package wrburnham.kafkaproducer.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wrburnham.kafkaproducer.model.KafkaProducerData;
import wrburnham.kafkaproducer.model.Tags;
import wrburnham.kafkaproducer.model.Tuple;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    public void save(KafkaProducerData data, File file) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element headers = document.createElement(Tags.headers);
        for (Tuple h : data.headers()) {
            Node node = document.createElement(Tags.header);
            Node key = document.createElement(Tags.headerKey);
            key.appendChild(document.createTextNode(h.key()));
            node.appendChild(key);
            Node value = document.createElement(Tags.headerValue);
            value.appendChild(document.createTextNode(h.value()));
            node.appendChild(value);
            headers.appendChild(node);
        }
        Element properties = document.createElement(Tags.properties);
        for (String prop : data.properties()) {
            Element property = document.createElement(Tags.property);
            property.appendChild(document.createTextNode(prop));
            properties.appendChild(property);
        }
        Element key = document.createElement(Tags.key);
        key.appendChild(document.createTextNode(data.key()));
        Element topic = document.createElement(Tags.topic);
        topic.appendChild(document.createTextNode(data.topic()));
        Element partition = document.createElement(Tags.partition);
        partition.appendChild(document.createTextNode(data.partition()));
        Element message = document.createElement(Tags.message);
        message.appendChild(document.createTextNode(data.message()));
        Element root = document.createElementNS("io.github.wrburnham", Tags.config);
        root.appendChild(headers);
        root.appendChild(properties);
        root.appendChild(key);
        root.appendChild(topic);
        root.appendChild(partition);
        root.appendChild(message);
        document.appendChild(root);
        DOMSource source = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            transformer.transform(source, new StreamResult(fos));
        }
    }


    public KafkaProducerData open(File file) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file);
        document.getDocumentElement().normalize();
        NodeList headers = document.getElementsByTagName(Tags.headers)
                .item(0)
                .getChildNodes();
        List<Tuple> dataHeaders = new ArrayList<>();
        for (int i = 0; i < headers.getLength(); i++) {
            Node header = headers.item(i);
            // TODO make this better
            if (!Tags.header.equals(header.getNodeName())) {
                throw new IllegalStateException(String.format("Expected a %s node", Tags.header));
            }
            NodeList children = header.getChildNodes();
            String key = null;
            String value = null;
            for (int j = 0; j < children.getLength(); j++) {
                Node node = children.item(j);
                if (Tags.headerKey.equals(node.getNodeName())) {
                    key = node.getTextContent();
                } else if (Tags.headerValue.equals(node.getNodeName())) {
                    value = node.getTextContent();
                }
            }
            if (key == null || value == null) {
                throw new IllegalStateException(String.format("Invalid values for headerKey %s and headerValue %s", key, value));
            }
            dataHeaders.add(new Tuple(key, value));
        }

        ArrayList<String> dataProperties = new ArrayList<>();
        NodeList properties = document.getElementsByTagName(Tags.properties)
                .item(0)
                .getChildNodes();
        for (int i = 0; i < properties.getLength(); i++) {
            Node property = properties.item(i);
            if (!Tags.property.equals(property.getNodeName())) {
                throw new IllegalStateException(String.format("Expected a %s node", Tags.property));
            }
            dataProperties.add(property.getTextContent());
        }
        String dataKey = document.getElementsByTagName(Tags.key)
                .item(0)
                .getTextContent();
        String dataPartition = document.getElementsByTagName(Tags.partition)
                .item(0)
                .getTextContent();
        String dataTopic = document.getElementsByTagName(Tags.topic)
                .item(0)
                .getTextContent();
        String dataMessage = document.getElementsByTagName(Tags.message)
                .item(0)
                .getTextContent();
        return new KafkaProducerData(
                dataProperties,
                dataHeaders,
                dataTopic,
                dataKey,
                dataPartition,
                dataMessage);
    }

}
