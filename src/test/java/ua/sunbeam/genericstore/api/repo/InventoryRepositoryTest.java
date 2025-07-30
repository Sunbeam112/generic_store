package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ua.sunbeam.genericstore.model.DAO.InventoryRepository;
import ua.sunbeam.genericstore.model.Inventory;
import ua.sunbeam.genericstore.model.Product;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create and persist a Product before each test
        // as Inventory has a @OneToOne relationship with Product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(10.00);
        testProduct.setDescription("A product for testing.");
        testProduct.setCategory("Electronics"); // Added this line to set a value for the non-nullable 'category'
        // You can optionally set other nullable fields like urlPhoto, subcategory, shortDescription, etc.,
        // but they are not strictly required for the Inventory test to pass unless you need to specifically
        // test scenarios related to those fields or if your database configuration enforces non-nullability
        // on them despite the entity not explicitly declaring it with nullable = false.
        entityManager.persistAndFlush(testProduct);
    }

    // ... rest of your test methods remain the same ...

    @Test
    void testSaveInventory() {
        Inventory inventory = new Inventory(testProduct, 100);
        Inventory savedInventory = inventoryRepository.save(inventory);

        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getQuantity()).isEqualTo(100);
        assertThat(savedInventory.getProduct().getId()).isEqualTo(testProduct.getId());
    }

    @Test
    void testFindById() {
        Inventory inventory = new Inventory(testProduct, 50);
        entityManager.persistAndFlush(inventory); // Persist to get an ID

        Optional<Inventory> foundInventory = inventoryRepository.findById(inventory.getId());

        assertThat(foundInventory).isPresent();
        assertThat(foundInventory.get().getQuantity()).isEqualTo(50);
        assertThat(foundInventory.get().getProduct().getId()).isEqualTo(testProduct.getId());
    }

    @Test
    void testUpdateInventory() {
        Inventory inventory = new Inventory(testProduct, 200);
        entityManager.persistAndFlush(inventory);

        inventory.setQuantity(250);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        assertThat(updatedInventory.getQuantity()).isEqualTo(250);
    }

    @Test
    void testDeleteInventory() {
        Inventory inventory = new Inventory(testProduct, 10);
        entityManager.persistAndFlush(inventory);

        inventoryRepository.deleteById(inventory.getId());

        Optional<Inventory> deletedInventory = inventoryRepository.findById(inventory.getId());
        assertThat(deletedInventory).isNotPresent();
    }

    @Test
    void testFindAll() {
        Product product2 = new Product();
        product2.setName("Test Product 2");
        product2.setPrice(20.00);
        product2.setDescription("Another product for testing.");
        product2.setCategory("Books"); // Added for the second product as well
        entityManager.persistAndFlush(product2);

        Inventory inventory1 = new Inventory(testProduct, 10);
        Inventory inventory2 = new Inventory(product2, 20);
        entityManager.persistAndFlush(inventory1);
        entityManager.persistAndFlush(inventory2);

        Iterable<Inventory> inventories = inventoryRepository.findAll();
        assertThat(inventories).hasSize(2);
    }
}