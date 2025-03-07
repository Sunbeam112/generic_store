package ua.sunbeam.genericstore.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<UserOrder, Long> {
}
