package ua.sunbeam.genericstore.service.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.sunbeam.genericstore.error.CsvProcessingException;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.ProductImage; // Import ProductImage

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCSVReader {

    private final ProductRepository productRepository;

    public ProductCSVReader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> importProductsFromCSV(MultipartFile file) throws CsvProcessingException, IllegalArgumentException {
        validateFile(file);

        try (CSVParser csvParser = createCsvParser(file)) {
            validateCsvHeaders(csvParser);
            return parseAndMapProducts(csvParser);
        } catch (IOException e) {
            throw new CsvProcessingException("Error reading or parsing CSV file: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format detected in CSV data. Please check numeric fields.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String originalFilename = file.getOriginalFilename();
        if (!"text/csv".equals(file.getContentType()) &&
                (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv"))) {
            throw new IllegalArgumentException("Invalid file type. Please upload a CSV file.");
        }
    }

    private CSVParser createCsvParser(MultipartFile file) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(CSVUtils.PRODUCT_EXPECTED_HEADERS)
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreHeaderCase(true)
                .build();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

        return new CSVParser(reader, csvFormat);
    }

    private void validateCsvHeaders(CSVParser csvParser) throws IOException {
        Map<String, Integer> headerMap = csvParser.getHeaderMap();

        if (headerMap == null || headerMap.isEmpty()) {
            throw new IOException("CSV file must contain a header row that matches expected format.");
        }

        Set<String> expectedHeaderSet = Arrays.stream(CSVUtils.PRODUCT_EXPECTED_HEADERS)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> actualHeaderSet = headerMap.keySet().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean allExpectedHeadersArePresent = actualHeaderSet.containsAll(expectedHeaderSet);
        if (!allExpectedHeadersArePresent) {
            String missingHeaders = expectedHeaderSet.stream()
                    .filter(expected -> !actualHeaderSet.contains(expected))
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Missing expected headers: " + missingHeaders + " in the CSV file.");
        }

        Set<String> unexpectedHeaders = actualHeaderSet.stream()
                .filter(actual -> !expectedHeaderSet.contains(actual))
                .collect(Collectors.toSet());

        if (!unexpectedHeaders.isEmpty()) {
            throw new IllegalArgumentException("Unexpected headers found: " + String.join(", ", unexpectedHeaders) + " in the CSV file.");
        }
    }

    private List<Product> parseAndMapProducts(CSVParser csvParser) throws IOException, NumberFormatException {
        List<Product> products = new ArrayList<>();
        int recordNumber = 0;
        for (CSVRecord csvRecord : csvParser) {
            recordNumber++;
            try {
                Product product = new Product();
                product.setName(csvRecord.get("name"));

                String priceString = csvRecord.get("price");
                if (priceString == null || priceString.trim().isEmpty()) {
                    throw new IllegalArgumentException("Price cannot be empty for record #" + recordNumber);
                }
                product.setPrice(Double.parseDouble(priceString));
                product.setDescription(csvRecord.get("description"));
                product.setCategory(csvRecord.get("category"));
                product.setUrlPhoto(csvRecord.get("url_photo"));
                product.setSubcategory(csvRecord.get("subcategory"));
                product.setShortDescription(csvRecord.get("short_description"));


                String productImageUrlsString = csvRecord.get("product_image_urls");
                if (productImageUrlsString != null && !productImageUrlsString.trim().isEmpty()) {
                    String[] imageUrls = productImageUrlsString.split(CSVUtils.PRODUCT_IMAGE_URL_DELIMITER);
                    List<ProductImage> productImages = new ArrayList<>();
                    int displayOrder = 1;
                    for (String url : imageUrls) {
                        if (!url.trim().isEmpty()) {
                            ProductImage productImage = new ProductImage();
                            productImage.setImageUrl(url.trim());
                            productImage.setProduct(product);
                            if(productImage.getDisplayOrder()==null) productImage.setDisplayOrder(displayOrder++);
                            productImages.add(productImage);
                        }
                    }
                    product.setProductImages(productImages);
                }

                products.add(product);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error in CSV record #" + recordNumber + ": " + e.getMessage(), e);
            }
        }
        return products;
    }
}