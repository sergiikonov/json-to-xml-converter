package profIT.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestDataGenerator {
    private static final String DIR_PATH = "json_data";
    private static final int FILES_COUNT = 10;
    private static final int RECORDS_PER_FILE = 100_000;

    public static void main(String[] args) {
        File dir = new File(DIR_PATH);
        if (!dir.exists()) dir.mkdir();

        System.out.println("Generating test data...");
        long start = System.currentTimeMillis();

        for (int i = 0; i < FILES_COUNT; i++) {
            generateFile(new File(dir, "data_" + i + ".json"));
            System.out.println("Generated file " + i);
        }

        long end = System.currentTimeMillis();
        System.out.println("Done in " + (end - start) + " ms");
    }

    private static void generateFile(File file) {
        Random random = new Random();
        String[] directors = {"Nolan", "Spielberg", "Tarantino", "Scorsese", "Villeneuve"};
        String[] genres = {"Drama", "Sci-Fi", "Action", "Comedy", "Thriller"};

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[\n");
            for (int i = 0; i < RECORDS_PER_FILE; i++) {
                String director = directors[random.nextInt(directors.length)];
                String genre1 = genres[random.nextInt(genres.length)];
                String genre2 = genres[random.nextInt(genres.length)];
                int year = 1990 + random.nextInt(35);

                String json = String.format(
                        "  {\"title\": \"Movie %d\", \"director\": \"%s\", \"release_year\": "
                                + "%d, \"genres\": [\"%s\", \"%s\"]}",
                        i, director, year, genre1, genre2
                );

                writer.write(json);
                if (i < RECORDS_PER_FILE - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]");
        } catch (IOException e) {
            System.err.println("Error writing to file " + file.getName() + ": " + e.getMessage());
        }
    }
}
