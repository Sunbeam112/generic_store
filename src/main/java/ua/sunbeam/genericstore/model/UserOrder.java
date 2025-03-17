package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "user_order")
@Getter
@Setter
@ToString
public class UserOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "payment_id")
    private String paymentId;


    @Column(name = "shipment_id")
    private String shipmentId;

    @JsonIgnore
    @OneToMany(mappedBy = "userOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderItem> orderItems;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "local_user_id", nullable = false)
    private LocalUser localUser;

    @Column(name = "status", length = 32)
    private String status;

}