package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.sunbeam.genericstore.model.UserOrder;

public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {

}