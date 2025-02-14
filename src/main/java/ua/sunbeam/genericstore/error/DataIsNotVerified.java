package ua.sunbeam.genericstore.error;

import java.util.List;
import java.util.Map;

public class DataIsNotVerified extends RuntimeException {
    Map<String, List<String>> errors;

    public DataIsNotVerified(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }


}
