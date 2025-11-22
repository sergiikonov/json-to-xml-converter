package profIT.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Comparator;
import java.util.Map;

public class XmlCreator {
    private static final String TAG_STATISTICS = "statistics";
    private static final String TAG_ITEM = "item";
    private static final String TAG_VALUE = "value";
    private static final String TAG_COUNT = "count";
    private static final String FILE_PREFIX = "statistic_by_";
    private static final String FILE_EXTENSION = ".xml";
    private static final String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";
    private static final String INDENT_SPACE = "4";

    public static void createStatisticsFile(Map<String, Long> statistics, String attribute)
            throws ParserConfigurationException, TransformerException {
        String fileName = FILE_PREFIX + attribute + FILE_EXTENSION;
        Document document = buildXmlDocument(statistics);
        writeXmlToFile(document, fileName);
        System.out.println("File successfully created: " + fileName);
    }

    private static Document buildXmlDocument(Map<String, Long> statistics)
            throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element rootElement = document.createElement(TAG_STATISTICS);
        document.appendChild(rootElement);

        statistics.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> appendItemElement(document, rootElement, entry));

        return document;
    }

    private static void appendItemElement(Document document, Element root, Map.Entry<String, Long> entry) {
        Element itemElement = document.createElement(TAG_ITEM);
        root.appendChild(itemElement);

        appendElementWithText(document, itemElement, TAG_VALUE, entry.getKey());
        appendElementWithText(document, itemElement, TAG_COUNT, String.valueOf(entry.getValue()));
    }

    private static void appendElementWithText(Document document, Element parent, String tagName, String text) {
        Element element = document.createElement(tagName);
        element.setTextContent(text);
        parent.appendChild(element);
    }

    private static void writeXmlToFile(Document document, String fileName)
            throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(INDENT_AMOUNT_KEY, INDENT_SPACE);

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(fileName));

        transformer.transform(source, result);
    }
}
