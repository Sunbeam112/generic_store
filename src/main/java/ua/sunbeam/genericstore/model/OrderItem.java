package ua.sunbeam.genericstore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_dispatched")
    private boolean isDispatched;

    @Column(name = "date_dispatched")
    private Timestamp dateDispatched;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private UserOrder userOrder;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}

