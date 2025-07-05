package ua.sunbeam.genericstore.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.sunbeam.genericstore.model.Address;
import ua.sunbeam.genericstore.model.OrderItem;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderRequestBody {
    private List<ProductToOrderBody> products;
    private Long addressID;
    private Long orderID;
    private Long userID;
}
