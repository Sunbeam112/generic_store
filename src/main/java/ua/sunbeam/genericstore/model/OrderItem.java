package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", insertable = false, updatable = false)
    private Long productId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_dispatched")
    private boolean isDispatched;

    @Column(name = "date_dispatched")
    private Timestamp dateDispatched;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private UserOrder userOrder;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


}

