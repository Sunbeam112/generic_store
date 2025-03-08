package ua.sunbeam.genericstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_order_id", nullable = false)
    private UserOrder userOrder;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_dispatched", nullable = false)
    private Boolean isDispatched = false;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Boolean getIsDispatched() {
        return isDispatched;
    }

    public void setIsDispatched(Boolean isDispatched) {
        this.isDispatched = isDispatched;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public UserOrder getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(UserOrder userOrder) {
        this.userOrder = userOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}