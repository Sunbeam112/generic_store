package ua.sunbeam.genericstore.service;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvFileUtil {

    // --- Constants ---
    private static final String CSV_FILE_PREFIX = "products_";
    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";
    private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType("text/csv");
    private static final String CONTENT_DISPOSITION_HEADER = "attachment";

    /**
     * Generates a unique CSV file name based on the current timestamp.
     * @return The generated file name.
     */
    public static String generateCsvFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        return CSV_FILE_PREFIX + timestamp + CSV_FILE_EXTENSION;
    }

    /**
     * Determines the directory to save the CSV file and creates it if it doesn't exist.
     * Defaults to the system's temporary directory if no specific directory is provided.
     * @param directoryPath The optional directory path provided by the user.
     * @param fileName The name of the file to be saved.
     * @return The full path where the CSV file should be saved.
     * @throws IOException If there's an error creating the directory.
     */
    public static Path determineAndCreateSaveDirectory(String directoryPath, String fileName) throws IOException {
        Path saveDirectory;
        if (directoryPath != null && !directoryPath.trim().isEmpty()) {
            saveDirectory = Paths.get(directoryPath);
        } else {
            saveDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        }
        Files.createDirectories(saveDirectory); // Ensure directory exists
        return saveDirectory.resolve(fileName);
    }

    /**
     * Reads all bytes from the specified file path.
     * @param filePath The path to the file to read.
     * @return A byte array containing the file's content.
     * @throws IOException If there's an error reading the file.
     */
    public static byte[] readAllBytes(Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

    /**
     * Deletes the file at the specified path.
     * @param filePath The path to the file to delete.
     * @throws IOException If there's an error deleting the file.
     */
    public static void deleteFile(Path filePath) throws IOException {
        Files.delete(filePath);
    }

    /**
     * Builds the HTTP response entity for the CSV file download.
     * @param csvBytes The byte array of the CSV content.
     * @param fileName The name of the CSV file.
     * @return A ResponseEntity containing the CSV file.
     */
    public static ResponseEntity<byte[]> buildCsvResponse(byte[] csvBytes, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CSV_MEDIA_TYPE);
        headers.setContentDispositionFormData(CONTENT_DISPOSITION_HEADER, fileName);
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}

