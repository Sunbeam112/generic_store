package ua.sunbeam.genericstore.api.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.io.Serializable;


@Getter
public class ProductToOrderBody implements Serializable {
    @NotNull(message = "Product ID cannot be null.")
    @Positive(message = "Product ID must be a positive number.")
    private Long productID;

    @NotNull(message = "Quantity cannot be null.")
    @Positive(message = "Quantity must be a positive number.")
    private Integer quantity;

    public ProductToOrderBody(Long productID, Integer quantity) {
        this.productID = productID;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductToOrderBody{" +
                "productID=" + productID +
                ", quantity=" + quantity +
                '}';
    }
}
