package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.OrderItem;

import java.util.List;

@Repository
public interface OrderItemsRepository extends CrudRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.userOrder.id = :orderId")
    List<OrderItem> getAllOrderItemsByOrderId(@Param("orderId") Long orderId);

}
