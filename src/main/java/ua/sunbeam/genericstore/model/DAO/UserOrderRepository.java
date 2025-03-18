package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.sunbeam.genericstore.model.UserOrder;

import java.util.List;

public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {

    List<UserOrder> getAllOrdersByUserIdOrderByDateAsc(Long userId);
}