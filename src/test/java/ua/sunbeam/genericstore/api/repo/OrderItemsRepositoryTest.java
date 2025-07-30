package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository; // Import the repository from its actual package

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class OrderItemsRepositoryTest {

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private TestEntityManager entityManager;

    private LocalUser testUser;
    private UserOrder testOrder1;
    private UserOrder testOrder2;
    private Product productA;
    private Product productB;
    private Product productC; // Add a third product for better isolation

    @BeforeEach
    void setUp() {
        // Ensure that each test runs in a clean state.
        // @DataJpaTest handles transaction rollback automatically.
        // We ensure entities are managed by TestEntityManager.

        testUser = new LocalUser();
        testUser.setEmail("user@example.com");
        testUser.setPassword("password");
        testUser.setEmailVerified(true);
        testUser = entityManager.persistAndFlush(testUser);

        // Save distinct Products for better isolation
        productA = new Product();
        productA.setName("Laptop Pro");
        productA.setPrice(1200.00);
        productA.setDescription("High-performance laptop.");
        productA.setCategory("Electronics");
        productA.setShortDescription("Powerful laptop for professionals.");
        productA = entityManager.persistAndFlush(productA);

        productB = new Product();
        productB.setName("Wireless Mouse");
        productB.setPrice(25.50);
        productB.setDescription("Ergonomic wireless mouse.");
        productB.setCategory("Accessories");
        productB.setShortDescription("Comfortable and precise mouse.");
        productB = entityManager.persistAndFlush(productB);

        // Add a third product to ensure productA is only linked to one order item for the delete test
        productC = new Product();
        productC.setName("Keyboard");
        productC.setPrice(75.00);
        productC.setDescription("Mechanical keyboard.");
        productC.setCategory("Accessories");
        productC.setShortDescription("Clicky and responsive keyboard.");
        productC = entityManager.persistAndFlush(productC);

        testOrder1 = new UserOrder();
        testOrder1.setLocalUser(testUser);
        testOrder1.setDate(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
        testOrder1.setStatus("PENDING");
        testOrder1.setPaymentId("PAY_ORD1");
        testOrder1.setShipmentId("SHIP_ORD1");
        testOrder1 = entityManager.persistAndFlush(testOrder1);

        testOrder2 = new UserOrder();
        testOrder2.setLocalUser(testUser);
        testOrder2.setDate(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        testOrder2.setStatus("COMPLETED");
        testOrder2.setPaymentId("PAY_ORD2");
        testOrder2.setShipmentId("SHIP_ORD2");
        testOrder2 = entityManager.persistAndFlush(testOrder2);

        // Save OrderItems for testOrder1
        OrderItem item1_order1 = new OrderItem();
        item1_order1.setUserOrder(testOrder1);
        item1_order1.setProduct(productA); // This is productA, will be deleted in testDeleteOrderItem
        item1_order1.setQuantity(1);
        item1_order1.setDispatched(false);
        entityManager.persistAndFlush(item1_order1);

        OrderItem item2_order1 = new OrderItem();
        item2_order1.setUserOrder(testOrder1);
        item2_order1.setProduct(productB); // This is productB
        item2_order1.setQuantity(2);
        item2_order1.setDispatched(true);
        item2_order1.setDateDispatched(Timestamp.valueOf(LocalDateTime.now().minusDays(4)));
        entityManager.persistAndFlush(item2_order1);

        // Save OrderItem for testOrder2 - now using productC to avoid conflict with productA in delete test
        OrderItem item1_order2 = new OrderItem();
        item1_order2.setUserOrder(testOrder2);
        item1_order2.setProduct(productC); // Changed to productC
        item1_order2.setQuantity(1);
        item1_order2.setDispatched(true);
        item1_order2.setDateDispatched(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        entityManager.persistAndFlush(item1_order2);

        entityManager.clear(); // Detach all entities for fresh reads in tests
    }


    @Test
    void testGetAllOrderItemsByOrderId_Found() {
        List<OrderItem> orderItems = orderItemsRepository.getAllOrderItemsByOrderId(testOrder1.getId());

        assertThat(orderItems)
                .isNotNull()
                .hasSize(2)
                .extracting(OrderItem::getProduct)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop Pro", "Wireless Mouse");

        assertThat(orderItems)
                .filteredOn(oi -> oi.getProduct().getId().equals(productA.getId()))
                .hasSize(1)
                .first()
                .satisfies(item1 -> {
                    assertThat(item1.getQuantity()).isEqualTo(1);
                    assertThat(item1.isDispatched()).isFalse();
                    assertThat(item1.getProduct().getName()).isEqualTo("Laptop Pro");
                });

        assertThat(orderItems)
                .filteredOn(oi -> oi.getProduct().getId().equals(productB.getId()))
                .hasSize(1)
                .first()
                .satisfies(item2 -> {
                    assertThat(item2.getQuantity()).isEqualTo(2);
                    assertThat(item2.isDispatched()).isTrue();
                    assertThat(item2.getProduct().getName()).isEqualTo("Wireless Mouse");
                });
    }

    @Test
    void testGetAllOrderItemsByOrderId_NoItemsForOrder() {
        UserOrder emptyOrder = new UserOrder();
        emptyOrder.setLocalUser(entityManager.find(LocalUser.class, testUser.getId()));
        emptyOrder.setDate(Timestamp.valueOf(LocalDateTime.now()));
        emptyOrder.setStatus("NEW");
        emptyOrder = entityManager.persistAndFlush(emptyOrder);
        entityManager.clear();

        List<OrderItem> orderItems = orderItemsRepository.getAllOrderItemsByOrderId(emptyOrder.getId());

        assertThat(orderItems)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void testGetAllOrderItemsByOrderId_NonExistentOrderId() {
        List<OrderItem> orderItems = orderItemsRepository.getAllOrderItemsByOrderId(999L);

        assertThat(orderItems)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void testSaveNewOrderItem() {
        OrderItem newItem = new OrderItem();
        newItem.setUserOrder(entityManager.find(UserOrder.class, testOrder2.getId()));
        newItem.setProduct(entityManager.find(Product.class, productB.getId()));
        newItem.setQuantity(3);
        newItem.setDispatched(false);

        OrderItem savedItem = orderItemsRepository.save(newItem);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedItem)
                .isNotNull()
                .extracting(OrderItem::getId).isNotNull();
        assertThat(savedItem.getQuantity()).isEqualTo(3);
        assertThat(savedItem.getUserOrder().getId()).isEqualTo(testOrder2.getId());
        assertThat(savedItem.getProduct().getId()).isEqualTo(productB.getId());

        Optional<OrderItem> foundItem = orderItemsRepository.findById(savedItem.getId());
        assertThat(foundItem)
                .isPresent()
                .map(OrderItem::getQuantity).contains(3);
    }

    @Test
    void testUpdateOrderItem() {
        OrderItem itemToUpdate = orderItemsRepository.getAllOrderItemsByOrderId(testOrder1.getId())
                .stream()
                .filter(oi -> oi.getProduct().getId().equals(productA.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item to update not found"));

        int originalQuantity = itemToUpdate.getQuantity();
        itemToUpdate.setQuantity(originalQuantity + 5);
        itemToUpdate.setDispatched(true);
        itemToUpdate.setDateDispatched(Timestamp.valueOf(LocalDateTime.now()));

        OrderItem updatedItem = orderItemsRepository.save(itemToUpdate);
        entityManager.flush();
        entityManager.clear();

        assertThat(updatedItem)
                .isNotNull()
                .returns(itemToUpdate.getId(), OrderItem::getId)
                .returns(originalQuantity + 5, OrderItem::getQuantity)
                .returns(true, OrderItem::isDispatched)
                .returns(itemToUpdate.getUserOrder().getId(), oi -> oi.getUserOrder().getId())
                .returns(itemToUpdate.getProduct().getId(), oi -> oi.getProduct().getId());

        assertThat(updatedItem.getDateDispatched()).isNotNull();

        Optional<OrderItem> fetchedUpdatedItem = orderItemsRepository.findById(itemToUpdate.getId());
        assertThat(fetchedUpdatedItem)
                .isPresent()
                .map(OrderItem::getQuantity).contains(originalQuantity + 5);
    }

    @Test
    void testDeleteOrderItem() {
        // Retrieve the item we want to delete, which refers to productA
        OrderItem itemToDelete = orderItemsRepository.getAllOrderItemsByOrderId(testOrder1.getId())
                .stream()
                .filter(oi -> oi.getProduct().getId().equals(productA.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item to delete not found"));

        Long idToDelete = itemToDelete.getId();
        orderItemsRepository.deleteById(idToDelete);
        entityManager.flush(); // Ensure the delete operation is committed to the in-memory DB

        // Now verify it's gone
        Optional<OrderItem> deletedItem = orderItemsRepository.findById(idToDelete);
        assertThat(deletedItem).isNotPresent();

        // Ensure other items for the same order (testOrder1) are still present
        List<OrderItem> remainingItems = orderItemsRepository.getAllOrderItemsByOrderId(testOrder1.getId());
        assertThat(remainingItems).hasSize(1);
        // Verify the remaining item is the one we didn't delete (productB)
        assertThat(remainingItems.get(0).getProduct().getName()).isEqualTo("Wireless Mouse");

        // The key fix: Because productA is no longer referenced by any order item,
        // if your Product entity has a cascade type (e.g., CascadeType.REMOVE) from OrderItem to Product,
        // or if a global cleanup mechanism tries to remove unreferenced products, this setup
        // will prevent the constraint violation. By giving productA only one OrderItem reference
        // that is then deleted, it becomes eligible for removal without conflicts.
    }

    @Test
    void testCountOrderItems() {
        long initialCount = orderItemsRepository.count();
        assertThat(initialCount).isEqualTo(3);

        OrderItem newItem = new OrderItem();
        newItem.setUserOrder(entityManager.find(UserOrder.class, testOrder1.getId()));
        newItem.setProduct(entityManager.find(Product.class, productA.getId()));
        newItem.setQuantity(10);
        orderItemsRepository.save(newItem);
        entityManager.flush();

        assertThat(orderItemsRepository.count()).isEqualTo(4);
    }
}