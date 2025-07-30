package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.sunbeam.genericstore.model.DAO.UserOrderRepository;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.UserOrder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class UserOrderRepositoryTest {

    @Autowired
    private UserOrderRepository userOrderRepository;

    @Autowired
    private UserRepository localUserRepository; // Assuming you have a LocalUserRepository to persist users

    private LocalUser testUser1;
    private LocalUser testUser2;

    @BeforeEach
    void setUp() {
        // Clear repositories before each test to ensure a clean state
        userOrderRepository.deleteAll();
        localUserRepository.deleteAll();

        // Create and save test users
        testUser1 = new LocalUser();
        testUser1.setEmail("testuser1@example.com");
        testUser1.setPassword("password123");
        testUser1.setEmailVerified(true);
        testUser1 = localUserRepository.save(testUser1);

        testUser2 = new LocalUser();
        testUser2.setEmail("testuser2@example.com");
        testUser2.setPassword("password456");
        testUser2.setEmailVerified(true);
        testUser2 = localUserRepository.save(testUser2);

        // Create and save test orders for testUser1
        UserOrder order1 = new UserOrder();
        order1.setLocalUser(testUser1);
        order1.setDate(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        order1.setStatus("PENDING");
        order1.setPaymentId("PAY123");
        order1.setShipmentId("SHIP123");
        userOrderRepository.save(order1);

        UserOrder order2 = new UserOrder();
        order2.setLocalUser(testUser1);
        order2.setDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        order2.setStatus("COMPLETED");
        order2.setPaymentId("PAY456");
        order2.setShipmentId("SHIP456");
        userOrderRepository.save(order2);

        UserOrder order3 = new UserOrder();
        order3.setLocalUser(testUser1);
        order3.setDate(Timestamp.valueOf(LocalDateTime.now()));
        order3.setStatus("SHIPPED");
        order3.setPaymentId("PAY789");
        order3.setShipmentId("SHIP789");
        userOrderRepository.save(order3);

        // Create and save a test order for testUser2
        UserOrder order4 = new UserOrder();
        order4.setLocalUser(testUser2);
        order4.setDate(Timestamp.valueOf(LocalDateTime.now().minusHours(5)));
        order4.setStatus("PENDING");
        order4.setPaymentId("PAYABC");
        order4.setShipmentId("SHIPABC");
        userOrderRepository.save(order4);
    }

    @Test
    void testGetAllOrdersByUserIdOrderByDateAsc() {
        // Retrieve orders for testUser1
        List<UserOrder> user1Orders = userOrderRepository.getAllOrdersByUserIdOrderByDateAsc(testUser1.getId());

        // Assertions
        Assertions.assertNotNull(user1Orders, "The list of orders should not be null.");
        Assertions.assertEquals(3, user1Orders.size(), "There should be 3 orders for testUser1.");

        // Verify the order by date ascending
        Assertions.assertTrue(user1Orders.get(0).getDate().before(user1Orders.get(1).getDate()), "Order 1 should be before Order 2.");
        Assertions.assertTrue(user1Orders.get(1).getDate().before(user1Orders.get(2).getDate()), "Order 2 should be before Order 3.");

        // Verify that all retrieved orders belong to testUser1
        for (UserOrder order : user1Orders) {
            Assertions.assertEquals(testUser1.getId(), order.getLocalUser().getId(), "All orders should belong to testUser1.");
        }

        // Retrieve orders for testUser2
        List<UserOrder> user2Orders = userOrderRepository.getAllOrdersByUserIdOrderByDateAsc(testUser2.getId());
        Assertions.assertNotNull(user2Orders, "The list of orders for testUser2 should not be null.");
        Assertions.assertEquals(1, user2Orders.size(), "There should be 1 order for testUser2.");
        Assertions.assertEquals(testUser2.getId(), user2Orders.get(0).getLocalUser().getId(), "The order should belong to testUser2.");

        // Test with a non-existent user ID
        List<UserOrder> nonExistentUserOrders = userOrderRepository.getAllOrdersByUserIdOrderByDateAsc(999L);
        Assertions.assertNotNull(nonExistentUserOrders, "The list for non-existent user should not be null.");
        Assertions.assertTrue(nonExistentUserOrders.isEmpty(), "There should be no orders for a non-existent user.");
    }
}