package ua.sunbeam.genericstore.service.csv;

import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.model.Product;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class ProductCSVWriter {
    public void writeToCSV(String filePath, List<Product> products) throws IllegalArgumentException, IOException {
        if (products == null || filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Products list and file path cannot be null or empty.");
        }

        Path path = Paths.get(filePath);

        String header = String.join(",", CSVUtils.PRODUCT_EXPECTED_HEADERS);


        List<String> productLines = products.stream()
                .map(ProductCSVWriter::toCSVLine)
                .toList();


        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardOpenOption.CREATE,       // Create the file if it doesn't exist
                StandardOpenOption.TRUNCATE_EXISTING // Truncate the file if it already exists (overwrite)
        )) {
            // Write the header
            writer.write(header);
            writer.newLine();

            for (String line : productLines) {
                writer.write(line);
                writer.newLine(); // Add a newline after each product record
            }
        }

    }

    /**
     * Converts a single Product object into a CSV formatted string.
     * The order of fields in the CSV line must match the order in PRODUCT_EXPECTED_HEADERS.
     * Fields are enclosed in double quotes if they contain commas or double quotes.
     * Double quotes within a field are escaped by doubling them.
     *
     * @param product The Product object to convert.
     * @return A CSV formatted string representation of the product.
     */
    private static String toCSVLine(Product product) {
        return String.join(",",
                escapeCsvField(product.getName()),
                escapeCsvField(String.valueOf(product.getPrice())),
                escapeCsvField(product.getDescription()),
                escapeCsvField(product.getCategory()),
                escapeCsvField(product.getUrlPhoto()),
                escapeCsvField(product.getSubcategory()),
                escapeCsvField(product.getShortDescription())
        );
    }

    /**
     * Escapes a field for CSV output.
     * Encloses the field in double quotes if it contains a comma, double quote, or newline.
     * Doubles any existing double quotes within the field.
     *
     * @param field The object to be converted to a string and then escaped.
     * @return The escaped string for CSV.
     */
    private static CharSequence escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        String data = String.valueOf(field);
        // Check for characters that require quoting: comma, double quote, newline, carriage return
        if (data.contains(",") || data.contains("\"") || data.contains("\n") || data.contains("\r")) {
            // Escape internal double quotes by doubling them
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }

}
