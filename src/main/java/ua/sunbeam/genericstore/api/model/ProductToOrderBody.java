package ua.sunbeam.genericstore.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class ProductToOrderBody implements Serializable {
    @NotNull
    @NotBlank
    private long productID;
    @NotNull
    @NotBlank
    @Size(min = 1)
    private int quantity;

    public long getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
