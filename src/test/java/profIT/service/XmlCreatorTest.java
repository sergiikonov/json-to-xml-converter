package profIT.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XmlCreatorTest {
    private final String testAttribute = "test_attr";
    private final String expectedFileName = "statistic_by_" + testAttribute + ".xml";

    @AfterEach
    void tearDown() {
        File file = new File(expectedFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void statisticsMap_createStatisticsFile_xmlIsCreatedAndSortedByCountDescending() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("LowValue", 1L);
        stats.put("HighValue", 10L);
        stats.put("MidValue", 5L);

        XmlCreator.createStatisticsFile(stats, testAttribute);

        File resultFile = new File(expectedFileName);
        assertTrue(resultFile.exists(), "XML file should be created");

        String content = Files.readString(resultFile.toPath());

        assertTrue(content.contains("<statistics>"));
        assertTrue(content.contains("<item>"));

        int indexHigh = content.indexOf("HighValue");
        int indexMid = content.indexOf("MidValue");
        int indexLow = content.indexOf("LowValue");

        assertTrue(indexHigh < indexMid, "HighValue should appear before MidValue");
        assertTrue(indexMid < indexLow, "MidValue should appear before LowValue");

        assertTrue(content.contains("<count>10</count>"));
        assertTrue(content.contains("<count>5</count>"));
        assertTrue(content.contains("<count>1</count>"));
    }

    @Test
    void emptyStatistics_createStatisticsFile_xmlIsCreatedWithEmptyRoot() throws Exception {
        Map<String, Long> emptyStats = new HashMap<>();
        XmlCreator.createStatisticsFile(emptyStats, testAttribute);
        File resultFile = new File(expectedFileName);
        assertTrue(resultFile.exists());

        String content = Files.readString(resultFile.toPath());
        assertTrue(content.contains("<statistics/>") || content.contains("<statistics></statistics>"),
                "Should contain empty root tag");
        assertFalse(content.contains("<item>"), "Should not contain items");
    }
}