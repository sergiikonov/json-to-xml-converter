package profIT.service;

import profIT.exception.StatsProcessingException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class JsonFileParser implements Runnable {
    private final File file;
    private final String attribute;
    private final ConcurrentHashMap<String, LongAdder> globalStats;

    public JsonFileParser(File file, String attribute, ConcurrentHashMap<String, LongAdder> globalStats) {
        this.file = file;
        this.attribute = attribute;
        this.globalStats = globalStats;
    }

    @Override
    public void run() {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(ObjectReadContext.empty(), file)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new StatsProcessingException("File " + file.getName() + " doesn't exist JSON array");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                processMovieObject(parser);
            }

        } catch (Exception e) {
            System.err.println("Critical error processing file " + file.getName() + ": " + e.getMessage());
        }
    }

    private void processMovieObject(JsonParser parser) throws Exception {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            if (attribute.equals(fieldName)) {
                extractValue(parser);
            } else {
                parser.skipChildren();
            }
        }
    }

    private void extractValue(JsonParser parser) throws Exception {
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                updateStats(parser.getValueAsString());
            }
        } else {
            updateStats(parser.getValueAsString());
        }
    }

    private void updateStats(String value) {
        if (value != null && !value.isBlank()) {
            globalStats.computeIfAbsent(value, k -> new LongAdder()).increment();
        }
    }
}
