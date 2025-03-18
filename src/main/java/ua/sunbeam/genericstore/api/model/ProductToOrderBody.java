package ua.sunbeam.genericstore.api.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ProductToOrderBody implements Serializable {
    @Positive
    private Long productID;
    @Positive
    private Integer quantity;

}
