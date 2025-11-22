package profIT;

import profIT.service.JsonFileParser;
import profIT.service.XmlCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final String DIRECTOR = "director";
    public static final String RELEASE_YEAR = "release_year";
    public static final String GENRES = "genres";
    private static final Set<String> ALLOWED_ATTRIBUTES = Set.of(DIRECTOR, RELEASE_YEAR, GENRES);

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int TIMEOUT_HOURS = 1;
    private static final String JSON_EXTENSION = ".json";

    public static void main(String[] args) {
        try {
            validateInput(args);

            String folderPath = args[0];
            String attribute = args[1];

            List<File> jsonFiles = findJsonFiles(folderPath);
            if (jsonFiles.isEmpty()) {
                System.out.println("No .json files found in directory: " + folderPath);
                return;
            }

            System.out.println("Found " + jsonFiles.size() + " files. Starting processing...");

            ConcurrentHashMap<String, LongAdder> rawStats = processFilesParallel(jsonFiles, attribute);
            Map<String, Long> finalStats = collectFinalStats(rawStats);

            if (finalStats.isEmpty()) {
                System.out.println("No data found for attribute: " + attribute);
                return;
            }

            XmlCreator.createStatisticsFile(finalStats, attribute);

        } catch (Exception e) {
            System.err.println("Critical execution error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void validateInput(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("2 arguments required: <folder_path> <attribute>");
        }

        String attribute = args[1];
        if (!ALLOWED_ATTRIBUTES.contains(attribute)) {
            throw new IllegalArgumentException("Unsupported attribute: " + attribute +
                    ". Allowed: " + ALLOWED_ATTRIBUTES);
        }
    }

    private static List<File> findJsonFiles(String folderPath) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(folderPath))) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(JSON_EXTENSION))
                    .map(Path::toFile)
                    .toList();
        }
    }

    private static ConcurrentHashMap<String, LongAdder> processFilesParallel(List<File> files, String attribute)
            throws InterruptedException {
        ConcurrentHashMap<String, LongAdder> globalStatistics = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (File file : files) {
            executor.submit(new JsonFileParser(file, attribute, globalStatistics));
        }

        executor.shutdown();
        if (!executor.awaitTermination(TIMEOUT_HOURS, TimeUnit.HOURS)) {
            executor.shutdownNow();
            throw new InterruptedException("Processing timeout exceeded");
        }

        return globalStatistics;
    }

    private static Map<String, Long> collectFinalStats(ConcurrentHashMap<String, LongAdder> rawStats) {
        return rawStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().sum()
                ));
    }
}
