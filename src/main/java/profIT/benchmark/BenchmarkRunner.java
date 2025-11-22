package profIT.benchmark;

import profIT.service.JsonFileParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class BenchmarkRunner {
    private static final String FOLDER_PATH = "json_data";
    private static final String ATTRIBUTE = "director";
    private static final String JSON_EXTENSION = ".json";

    public static void main(String[] args) throws IOException {
        List<File> files = findJsonFiles(FOLDER_PATH);

        if (files.isEmpty()) {
            System.err.println("Files not found! Run TestDataGenerator first.");
            return;
        }

        System.out.println("Starting Benchmark on " + files.size() + " files...");
        System.out.println("--------------------------------------------------");

        int[] threadCounts = {1, 2, 4, 8};

        for (int threads : threadCounts) {
            runTest(files, threads);
        }
    }

    private static void runTest(List<File> files, int nThreads) {
        System.out.print("Testing with " + nThreads + " thread(s)... ");
        System.gc();

        ConcurrentHashMap<String, LongAdder> stats = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        long startTime = System.currentTimeMillis();

        for (File file : files) {
            executor.submit(new JsonFileParser(file, ATTRIBUTE, stats));
        }
        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error: The execution was interrupted while waiting for tasks to complete.");
            System.err.println("Details: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Time: " + duration + " ms");
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
}
