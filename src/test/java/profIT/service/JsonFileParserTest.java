package profIT.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileParserTest {
    @TempDir
    Path tempDir;

    private ConcurrentHashMap<String, LongAdder> globalStats;

    @BeforeEach
    void setUp() {
        globalStats = new ConcurrentHashMap<>();
    }

    @Test
    void simpleAttributeJson_run_statisticsAreAggregatedCorrectly() throws IOException {
        File jsonFile = tempDir.resolve("movies.json").toFile();
        String jsonContent = """
                [
                  {"title": "Movie A", "director": "Director X"},
                  {"title": "Movie B", "director": "Director X"},
                  {"title": "Movie C", "director": "Director Y"}
                ]
                """;
        Files.writeString(jsonFile.toPath(), jsonContent);

        JsonFileParser parser = new JsonFileParser(jsonFile, "director", globalStats);
        parser.run();

        assertEquals(2, globalStats.size());
        assertEquals(2, globalStats.get("Director X").sum());
        assertEquals(1, globalStats.get("Director Y").sum());
    }

    @Test
    void arrayAttributeJson_run_arrayElementsAreAggregatedCorrectly() throws IOException {
        File jsonFile = tempDir.resolve("genres.json").toFile();
        String jsonContent = """
                [
                  {"title": "A", "genres": ["Drama", "Sci-Fi"]},
                  {"title": "B", "genres": ["Drama"]},
                  {"title": "C", "genres": []}
                ]
                """;
        Files.writeString(jsonFile.toPath(), jsonContent);

        JsonFileParser parser = new JsonFileParser(jsonFile, "genres", globalStats);
        parser.run();

        assertEquals(2, globalStats.size());
        assertEquals(2, globalStats.get("Drama").sum());
        assertEquals(1, globalStats.get("Sci-Fi").sum());
    }

    @Test
    void invalidJsonContent_run_noExceptionIsThrownAndStatsEmpty() throws IOException {
        File jsonFile = tempDir.resolve("broken.json").toFile();
        Files.writeString(jsonFile.toPath(), "{ NOT A JSON ARRAY }");

        JsonFileParser parser = new JsonFileParser(jsonFile, "director", globalStats);

        assertDoesNotThrow(parser::run);
        assertTrue(globalStats.isEmpty());
    }

    @Test
    void jsonMissingAttribute_run_statisticsRemainEmpty() throws IOException {
        File jsonFile = tempDir.resolve("empty.json").toFile();
        String jsonContent = "[{\"title\": \"No Director Here\"}]";
        Files.writeString(jsonFile.toPath(), jsonContent);

        JsonFileParser parser = new JsonFileParser(jsonFile, "director", globalStats);
        parser.run();

        assertTrue(globalStats.isEmpty());
    }
}
