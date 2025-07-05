package ua.sunbeam.genericstore.api.security;

import org.springframework.stereotype.Component;

@Component
public class UUIDUtils {


    public static String addDashesToUUIDString(String uuidStringNoDashes) {
        return java.util.UUID.fromString(
                uuidStringNoDashes
                        .replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                        )
        ).toString();
    }

    public static String generateUUIDWithoutDashes() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
