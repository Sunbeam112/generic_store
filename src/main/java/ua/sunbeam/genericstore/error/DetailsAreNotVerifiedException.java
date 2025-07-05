package ua.sunbeam.genericstore.error;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class DetailsAreNotVerifiedException extends RuntimeException {
    private final Map<String, List<String>> errors;

    public DetailsAreNotVerifiedException(Map<String, List<String>> errors) {
        this.errors = errors;
    }


}
