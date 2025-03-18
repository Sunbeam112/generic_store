package ua.sunbeam.genericstore.api.controller;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidationErrorsParser {

    public Map<String, List<String>> parseErrorsFrom(BindingResult bindingResult) {
        Map<String, List<String>> errorsMap = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            if (errorsMap.containsKey(fieldError.getField())) {
                List<String> values = errorsMap.get(fieldError.getField());
                values.add(fieldError.getDefaultMessage());
            } else {

                List<String> values = new ArrayList<>();
                values.add(fieldError.getDefaultMessage());
                errorsMap.put(fieldError.getField(), values);
            }

        }
        return errorsMap;

    }


}
