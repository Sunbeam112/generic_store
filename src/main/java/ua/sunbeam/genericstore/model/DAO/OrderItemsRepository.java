package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.OrderItems;

@Repository
public interface OrderItemsRepository extends CrudRepository<OrderItems, Long> {
}
