package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.Inventory;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
}
