package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "product")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "description", nullable = false, length = 16384)
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "url_photo", length = 2048)
    private String urlPhoto;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "short_description", length = 1024)
    private String shortDescription;

    @JsonIgnore
    @OneToOne(mappedBy = "product", cascade = CascadeType.REMOVE, optional = false, orphanRemoval = true)
    private transient Inventory inventory;


}